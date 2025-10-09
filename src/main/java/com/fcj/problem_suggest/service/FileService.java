package com.fcj.problem_suggest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private static final String INPUT_PREFIX = "input/";
    private static final String OUTPUT_PREFIX = "output/";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String MD_EXTENSION = ".md";
    private static final int MAX_TOKENS = 8196;
    private static final float TEMPERATURE = 0.7f;
    
    private final BedrockRuntimeClient bedrockClient;
    private final S3Client s3Client;
    private static final String CLAUDE_PROMPT = """
            # Document Processing Instructions
            ## Task Convert the provided file content into a clean, correctly formatted Markdown block.
            ## Processing Steps
            ### Step 1: Extract Raw Content
            - Extract ALL text, formulas, and visual elements from the file
            - Carefully scan the ENTIRE document to ensure NO illustrations, diagrams, or figures are missed
            - Pay special attention to ALL visual elements embedded within problem statements
            - For ANY problem involving spatial relationships, geometric scenarios, physical systems (like radars, trajectories), or data visualizations, ASSUME there should be a figure unless explicitly confirmed otherwise
            - If there are solutions/anything not related to the problem statement, stop scanning and continue with the next step.
            - Do not change the input language when output.
            ### Step 2: Isolate Core Content
            - Remove all headers, footers, page numbers, and watermarks
            - Retain only numbered/bulleted questions and their corresponding answer choices (A, B, C, D)
            - Preserve the relationship between text and ALL associated visual elements - When a problem describes a geometric setup, physical scenario, or references positions/movement in space, include a figure placeholder even if no actual image is visible in the PDF
            ### Step 3: Format and Correct
            - Don't add the "Mã đề" or "Trang" inside the text.
            - Don't add <Câu_number of problem> to the problem statement that are missing <Câu_number of problem>.
            - Convert all mathematical formulas to inline LaTeX using \\(...\\) delimiters
            - Keep the problems statement without a starter "Câu" and also if the question ended abruptly also keep the original content - Proofread and fix spelling/typographical errors in regular text only (DO NOT modify LaTeX content)
            - Format all question statements (e.g., "Question 1:") as Header Level 3 (###)
            - Place each answer option on its own separate line by adding 2 spaces then enter
            - Replace ALL figures/sketches/illustrations/diagrams/visual contents with sequential placeholders (figure_1, figure_2, etc.) followed by a line break
            - Insert figure placeholders IMMEDIATELY after the text that references them or where they appear in the original document
            - For problems involving 3D coordinates, paths, trajectories, or spatial relationships, ALWAYS include a figure placeholder even if the image is not clearly visible ## Output Format Provide only the final Markdown block without any explanations or additional text.
            - For each table in the PDF, turn it into a table in markdown. - Double-check that ALL visual elements have been properly represented with placeholders. - Before submitting, validate that every problem describing geometric or physical scenarios has appropriate figure placeholders.
            """;
    private static final String MODEL_ID = "us.anthropic.claude-3-7-sonnet-20250219-v1:0";

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public FileService(BedrockRuntimeClient bedrockClient, S3Client s3Client) {
        this.bedrockClient = bedrockClient;
        this.s3Client = s3Client;
    }

    public String processInputFiles() {
        try {
            var response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName).prefix(INPUT_PREFIX).build());
            
            for (var object : response.contents()) {
                if (!object.key().equals(INPUT_PREFIX)) {
                    processFile(object.key());
                }
            }
            return "Processing completed";
        } catch (Exception e) {
            throw new FileProcessingException("Failed to process input files", e);
        }
    }
    
    private void processFile(String key) {
        try {
            byte[] fileContent = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName).key(key).build()).readAllBytes();
        
        ContentBlock textContent = ContentBlock.builder()
            .text(CLAUDE_PROMPT)
            .build();
            
        Message message;
            if (key.toLowerCase().endsWith(PDF_EXTENSION)) {
            ContentBlock documentContent = ContentBlock.builder()
                .document(DocumentBlock.builder()
                    .format(DocumentFormat.PDF)
                    .name(sanitizeFilename(key.substring(key.lastIndexOf('/') + 1)))
                    .source(DocumentSource.builder()
                        .bytes(SdkBytes.fromByteArray(fileContent))
                        .build())
                    .build())
                .build();
            message = Message.builder()
                .role(ConversationRole.USER)
                .content(textContent, documentContent)
                .build();
        } else {
            ImageFormat imageFormat = getImageFormat(key);
            ContentBlock imageContent = ContentBlock.builder()
                .image(ImageBlock.builder()
                    .format(imageFormat)
                    .source(ImageSource.builder()
                        .bytes(SdkBytes.fromByteArray(fileContent))
                        .build())
                    .build())
                .build();
            message = Message.builder()
                .role(ConversationRole.USER)
                .content(textContent, imageContent)
                .build();
        }
            
            ConverseRequest request = ConverseRequest.builder()
                .modelId(MODEL_ID)
                .messages(message)
                .inferenceConfig(InferenceConfiguration.builder()
                    .maxTokens(MAX_TOKENS)
                    .temperature(TEMPERATURE)
                    .build())
                .build();
            
            ConverseResponse response = bedrockClient.converse(request);
                
            String generatedText = response.output().message().content().get(0).text();
            
            String fileName = key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'));
            String outputKey = OUTPUT_PREFIX + fileName + MD_EXTENSION;
            s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName).key(outputKey).build(), 
                RequestBody.fromString(generatedText));
        } catch (Exception e) {
            throw new FileProcessingException("Failed to process file: " + key, e);
        }
    }

    public void deleteInputPdf(String pdfName) {
        try {
            String key = INPUT_PREFIX + pdfName + PDF_EXTENSION;
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName).key(key).build());
        } catch (Exception e) {
            logger.warn("Failed to delete input PDF {}: {}", pdfName, e.getMessage());
        }
    }
    
    private ImageFormat getImageFormat(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) return ImageFormat.PNG;
        if (lower.endsWith(".gif")) return ImageFormat.GIF;
        if (lower.endsWith(".webp")) return ImageFormat.WEBP;
        return ImageFormat.JPEG;
    }
    
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\s\\-\\(\\)\\[\\]]", "")
                      .replaceAll("\\s+", " ")
                      .trim();
    }
    
    private static class FileProcessingException extends RuntimeException {
        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}