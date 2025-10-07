package com.fcj.problem_suggest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
public class EmbeddingService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TITAN_MODEL_ID = "amazon.titan-embed-text-v2:0";

    public EmbeddingService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
    }

    public float[] embedStatement(String problemStatement) throws Exception {
        try {
            String escapedText = problemStatement
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
                
            String requestBody = String.format(
                "{\"inputText\": \"%s\", \"dimensions\": 1024}",
                escapedText
            );
            
            InvokeModelResponse response = bedrockClient.invokeModel(InvokeModelRequest.builder()
                .modelId(TITAN_MODEL_ID)
                .body(SdkBytes.fromUtf8String(requestBody))
                .build());
                
            JsonNode responseJson = objectMapper.readTree(response.body().asUtf8String());
            JsonNode embeddingArray = responseJson.get("embedding");
            
            if (embeddingArray == null) {
                throw new RuntimeException("Invalid response from Titan Embedding: missing 'embedding' field");
            }
            
            float[] embedding = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = (float) embeddingArray.get(i).asDouble();
            }
            
            return embedding;
        } catch (software.amazon.awssdk.services.bedrockruntime.model.BedrockRuntimeException e) {
            if (e.statusCode() == 403) {
                throw new RuntimeException("Access denied to Titan Embedding model. Check IAM permissions for amazon.titan-embed-text-v2:0", e);
            }
            throw new RuntimeException("Bedrock API error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding for problem statement: " + e.getMessage(), e);
        }
    }
}