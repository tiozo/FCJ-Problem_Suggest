package com.fcj.problem_suggest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class SampleFileUploader {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleFileUploader.class);
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    public SampleFileUploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    public String uploadSampleFiles() {
        try {
            uploadPdfIfExists();
            uploadMarkdownIfExists();
            return "Sample files uploaded successfully";
        } catch (Exception e) {
            return "Failed to upload sample files: " + e.getMessage();
        }
    }
    
    private void uploadPdfIfExists() throws IOException {
        Path pdfPath = Paths.get("sample-files/input/sample.pdf");
        if (Files.exists(pdfPath)) {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("input/sample.pdf")
                .contentType("application/pdf")
                .build();
            
            s3Client.putObject(request, RequestBody.fromFile(pdfPath));
            logger.info("Uploaded sample.pdf to S3");
        } else {
            logger.warn("sample.pdf not found in sample-files/input/");
        }
    }
    
    private void uploadMarkdownIfExists() throws IOException {
        Path mdPath = Paths.get("sample-files/output/sample.md");
        if (Files.exists(mdPath)) {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("output/sample.md")
                .contentType("text/markdown")
                .build();
            
            s3Client.putObject(request, RequestBody.fromFile(mdPath));
            logger.info("Uploaded sample.md to S3");
        } else {
            logger.warn("sample.md not found in sample-files/output/");
        }
    }
}