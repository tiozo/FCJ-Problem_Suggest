package com.fcj.problem_suggest.controller;

import com.fcj.problem_suggest.service.MarkdownProcessor;
import com.fcj.problem_suggest.service.ToQdrant;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/*
* This Controller is used to test Qdrant input manually.
* */
@RestController
public class TestController {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TestController.class);
    private final MarkdownProcessor mdp;

    public TestController(ToQdrant mdp) {
        this.mdp = mdp;
    }

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "text/markdown"
    );

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File is empty!");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Only Markdown is allowed!");
        }

        try {
            String content = new String(file.getBytes());
            mdp.process(content);
            return ResponseEntity.ok("Markdown processed successfully");
        } catch (RuntimeException e) {
            logger.error("Failed to process markdown: {}", e.getMessage());
            if (e.getMessage().contains("not authorized") || e.getMessage().contains("403")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: Missing IAM permissions for Bedrock services - " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Processing failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to read file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File read failed: " + e.getMessage());
        }
    }
}
