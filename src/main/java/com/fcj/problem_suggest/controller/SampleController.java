package com.fcj.problem_suggest.controller;

import com.fcj.problem_suggest.service.SampleFileUploader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ver0.0.1/sample")
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class SampleController {
    
    private final SampleFileUploader uploader;
    
    public SampleController(SampleFileUploader uploader) {
        this.uploader = uploader;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadSampleFiles() {
        String result = uploader.uploadSampleFiles();
        return ResponseEntity.ok(result);
    }
}