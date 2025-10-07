package com.fcj.problem_suggest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Service
public class QdrantService {
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String QDRANT_URL = "http://localhost:6333";
    private static final String COLLECTION_NAME = "problems";

    public void createCollectionIfNotExists() throws Exception {
        // Check if collection exists
        HttpRequest checkRequest = HttpRequest.newBuilder()
            .uri(URI.create(QDRANT_URL + "/collections/" + COLLECTION_NAME))
            .GET()
            .build();
            
        HttpResponse<String> checkResponse = httpClient.send(checkRequest, HttpResponse.BodyHandlers.ofString());
        
        if (checkResponse.statusCode() == 404) {
            // Create collection
            Map<String, Object> vectorConfig = Map.of(
                "size", 1024,
                "distance", "Cosine"
            );
            Map<String, Object> collectionConfig = Map.of("vectors", vectorConfig);
            
            HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(QDRANT_URL + "/collections/" + COLLECTION_NAME))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(collectionConfig)))
                .build();
                
            httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        }
    }

    public void addPoint(float[] vector, String problemStatement, String problemAnswer) throws Exception {
        createCollectionIfNotExists();
        Map<String, Object> payload = Map.of(
            "problem_statement", problemStatement,
            "problem_tags", "",
            "problem_answer", problemAnswer
        );
        
        Map<String, Object> point = Map.of(
            "id", UUID.randomUUID().toString(),
            "vector", vector,
            "payload", payload
        );
        
        Map<String, Object> request = Map.of("points", new Object[]{point});
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(QDRANT_URL + "/collections/" + COLLECTION_NAME + "/points"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
            .build();
            
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}