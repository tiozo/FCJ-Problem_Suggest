package com.fcj.problem_suggest.controller;

import com.fcj.problem_suggest.service.S3MarkdownProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ver0.0.1/process")
public class ProcessController {
    
    private final S3MarkdownProcessor s3MarkdownProcessor;
    
    public ProcessController(S3MarkdownProcessor s3MarkdownProcessor) {
        this.s3MarkdownProcessor = s3MarkdownProcessor;
    }
    
    @PostMapping("/markdown/{pdfName}")
    public ResponseEntity<String> processMarkdown(@PathVariable String pdfName) {
        try {
            s3MarkdownProcessor.processMarkdownFromS3(pdfName);
            return ResponseEntity.ok("Successfully processed " + pdfName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to process " + pdfName + ": " + e.getMessage());
        }
    }
}