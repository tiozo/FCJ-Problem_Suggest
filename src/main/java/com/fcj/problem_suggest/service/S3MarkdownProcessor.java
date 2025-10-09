package com.fcj.problem_suggest.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Service
public class S3MarkdownProcessor {

    private static final Logger logger = LoggerFactory.getLogger(S3MarkdownProcessor.class);
    private static final String OUTPUT_PREFIX = "output/";
    private static final String INPUT_PREFIX = "input/";
    private static final String FIGURES_PREFIX = "figures/";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String MD_EXTENSION = ".md";
    private static final String PNG_EXTENSION = ".png";
    private static final String PYTHON_SCRIPT_PATH = "src/main/resources/python-script/extract_pdf_figures.py";
    
    private final S3Client s3Client;
    private final FileService fs;
    private final ToDB toDB;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    public S3MarkdownProcessor(S3Client s3Client, FileService fs, ToDB toDB) {
        this.s3Client = s3Client;
        this.fs = fs;
        this.toDB = toDB;
    }
    
    public void processMarkdownFromS3(String pdfName) {
        try {
            String markdownKey = OUTPUT_PREFIX + pdfName + MD_EXTENSION;
            String markdown = downloadFromS3(markdownKey);

            String pdfKey = INPUT_PREFIX + pdfName + PDF_EXTENSION;
            Path localPdfPath = downloadPdfToLocal(pdfKey, pdfName);

            extractFiguresAndUploadToS3(localPdfPath.toString(), pdfName);

            toDB.process(markdown, pdfName);

            deleteMarkdownFromS3(markdownKey);

            cleanupLocalFiles(pdfName);

        } catch (Exception e) {
            logger.error("Failed to process markdown from S3: {}", e.getMessage());
            throw new S3ProcessingException("Failed to process markdown from S3", e);
        }
    }
    
    public void deleteMarkdownFromS3(String markdownKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(markdownKey)
                .build();
                
            s3Client.deleteObject(request);
        } catch (Exception e) {
            logger.warn("Failed to delete markdown from S3 (continuing anyway): {}", e.getMessage());
        }
    }
    
    private String downloadFromS3(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            return s3Client.getObjectAsBytes(request).asUtf8String();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download " + key + " from S3: " + e.getMessage(), e);
        }
    }
    
    private Path downloadPdfToLocal(String s3Key, String pdfName) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
                
            byte[] pdfBytes = s3Client.getObjectAsBytes(request).asByteArray();
            Path localPath = Paths.get(pdfName + PDF_EXTENSION);
            Files.write(localPath, pdfBytes);
            return localPath;
        } catch (Exception e) {
            throw new S3ProcessingException("Failed to download PDF " + s3Key + " from S3", e);
        }
    }
    
    private void extractFiguresAndUploadToS3(String pdfPath, String pdfName) throws IOException, InterruptedException {
        // Run Python script to extract figures
        ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT_PATH, pdfPath);
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new S3ProcessingException("Python script failed with exit code: " + exitCode);
        }
        
        // Upload extracted figures to S3
        Path figureDir = Paths.get(pdfName);
        if (Files.exists(figureDir)) {
            try (var stream = Files.walk(figureDir)) {
                stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(PNG_EXTENSION))
                    .forEach(figurePath -> {
                        try {
                            uploadFigureToS3(figurePath, pdfName);
                        } catch (Exception e) {
                            logger.error("Failed to upload: {} - {}", figurePath, e.getMessage());
                            throw new S3ProcessingException("Failed to upload figure: " + figurePath, e);
                        }
                    });
            }
        }

        fs.deleteInputPdf(pdfName);
    }
    
    private void uploadFigureToS3(Path figurePath, String pdfName) {
        if (!Files.exists(figurePath)) {
            throw new S3ProcessingException("Figure file does not exist: " + figurePath);
        }
        
        try {
            String fileName = figurePath.getFileName().toString();
            String s3Key = FIGURES_PREFIX + pdfName + "/" + fileName;
            
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("image/png")
                .build();
                
            s3Client.putObject(request, RequestBody.fromFile(figurePath));
            
        } catch (Exception e) {
            throw new S3ProcessingException("Failed to upload figure to S3: " + figurePath, e);
        }
    }
    
    private void cleanupLocalFiles(String pdfName) throws IOException {
        // Delete local PDF
        Files.deleteIfExists(Paths.get(pdfName + PDF_EXTENSION));
        
        // Delete figure directory
        Path figureDir = Paths.get(pdfName);
        if (Files.exists(figureDir)) {
            try (var stream = Files.walk(figureDir)) {
                stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.debug("Failed to delete file during cleanup: {}", path);
                        }
                    });
            }
        }
    }
    
    private static class S3ProcessingException extends RuntimeException {
        public S3ProcessingException(String message) {
            super(message);
        }
        
        public S3ProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}