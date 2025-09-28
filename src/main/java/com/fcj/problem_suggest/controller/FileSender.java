package com.fcj.problem_suggest.controller;

import com.fcj.problem_suggest.service.FileSenderService;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ver0.0.1/file_send")
public class FileSender {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(FileSender.class);
    private final FileSenderService fss;

    public FileSender (FileSenderService fss) {
        this.fss = fss;
    }

    @PostMapping("/process")
    public String processFiles() {
        try {
            return fss.processInputFiles();
        } catch (Exception e) {
            logger.error("Processing failed: {}", e.getMessage());
            return "Processing failed: " + e.getMessage();
        }
    }
}
