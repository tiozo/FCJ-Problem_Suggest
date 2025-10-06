package com.fcj.problem_suggest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class FileSenderService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final S3Client s3Client;
    private static final String claudePrompt =
                    "# Document Processing Instructions \n" +
                    "## Task Convert the provided file content into a clean, correctly formatted Markdown block. \n" +
                    "## Processing Steps \n" +
                    "### Step 1: Extract Raw Content \n" +
                    "- Extract ALL text, formulas, and visual elements from the file \n" +
                    "- Carefully scan the ENTIRE document to ensure NO illustrations, diagrams, or figures are missed \n" +
                    "- Pay special attention to ALL visual elements embedded within problem statements \n" +
                    "- For ANY problem involving spatial relationships, geometric scenarios, physical systems (like radars, trajectories), or data visualizations, ASSUME there should be a figure unless explicitly confirmed otherwise \n" +
                    "- If there are solutions/anything not related to the problem statement, stop scanning and continue with the next step.\n" +
                    "- Do not change the input language when output. \n" +
                    "### Step 2: Isolate Core Content \n" +
                    "- Remove all headers, footers, page numbers, and watermarks \n" +
                    "- Retain only numbered/bulleted questions and their corresponding answer choices (A, B, C, D) \n" +
                    "- Preserve the relationship between text and ALL associated visual elements - When a problem describes a geometric setup, physical scenario, or references positions/movement in space, include a figure placeholder even if no actual image is visible in the PDF \n" +
                    "### Step 3: Format and Correct \n" +
                    "- Don't add the \"Mã đề\" or “Trang” inside the text. \n" +
                    "- Don't add <Câu_number of problem> to the problem statement that are missing <Câu_number of problem>.\n" +
                    "- Convert all mathematical formulas to inline LaTeX using \\(...\\) delimiters \n" +
                    "- Keep the problems statement without a starter \"Câu\" and also if the question ended abruptly also keep the original content - Proofread and fix spelling/typographical errors in regular text only (DO NOT modify LaTeX content) \n" +
                    "- Format all question statements (e.g., \"Question 1:\") as Header Level 3 (###) \n" +
                    "- Place each answer option on its own separate line by adding 2 spaces then enter \n" +
                    "- Replace ALL figures/sketches/illustrations/diagrams/visual contents with sequential placeholders (figure_1, figure_2, etc.) followed by a line break \n" +
                    "- Insert figure placeholders IMMEDIATELY after the text that references them or where they appear in the original document \n" +
                    "- For problems involving 3D coordinates, paths, trajectories, or spatial relationships, ALWAYS include a figure placeholder even if the image is not clearly visible ## Output Format Provide only the final Markdown block without any explanations or additional text. \n" +
                    "- For each table in the PDF, turn it into a table in markdown. - Double-check that ALL visual elements have been properly represented with placeholders. - Before submitting, validate that every problem describing geometric or physical scenarios has appropriate figure placeholders.\n";
    private static final String MODEL_ID = "us.anthropic.claude-3-7-sonnet-20250219-v1:0";

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public FileSenderService(BedrockRuntimeClient bedrockClient, S3Client s3Client) {
        this.bedrockClient = bedrockClient;
        this.s3Client = s3Client;
    }

    public String processInputFiles() throws Exception {
        var response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
            .bucket(bucketName).prefix("input/").build());
        
        for (var object : response.contents()) {
            if (!object.key().equals("input/")) {
                processFile(object.key());
            }
        }
        return "Processing completed";
    }
    
    private void processFile(String key) throws Exception {
        byte[] fileContent = s3Client.getObject(GetObjectRequest.builder()
            .bucket(bucketName).key(key).build()).readAllBytes();
        
        ContentBlock textContent = ContentBlock.builder()
            .text(claudePrompt)
            .build();
            
        Message message;
        if (key.toLowerCase().endsWith(".pdf")) {
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
                .maxTokens(8196)
                .temperature(0.7f)
                .build())
            .build();
            
        ConverseResponse response = bedrockClient.converse(request);
            
        String generatedText = response.output().message().content().get(0).text();
        
        String fileName = key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String outputKey = "output/" + timestamp + "_" + fileName + ".md";
        s3Client.putObject(PutObjectRequest.builder()
            .bucket(bucketName).key(outputKey).build(), 
            RequestBody.fromString(generatedText));
            
        s3Client.deleteObject(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
            .bucket(bucketName).key(key).build());
    }
    
    private ImageFormat getImageFormat(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) return ImageFormat.PNG;
        if (lower.endsWith(".gif")) return ImageFormat.GIF;
        if (lower.endsWith(".webp")) return ImageFormat.WEBP;
        return ImageFormat.JPEG; // default for jpg, jpeg, bmp
    }
    
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\s\\-\\(\\)\\[\\]]", "")
                      .replaceAll("\\s+", " ")
                      .trim();
    }
}