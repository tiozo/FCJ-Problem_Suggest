package com.fcj.problem_suggest.controller;

import com.fcj.problem_suggest.service.PdfSniper;
import com.fcj.problem_suggest.service.StorageService;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ver0.0.1/file-receiver")
public class FileReceiver {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(FileReceiver.class);
    private final StorageService ss;
    private final PdfSniper pdfSniper;

    public FileReceiver (StorageService ss, PdfSniper pdfSniper) {
        this.ss = ss;
        this.pdfSniper = pdfSniper;
    }

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf", "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("File is empty!");
        }
        
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Only PDF and image files are allowed!");
        }
        
        try {
            ss.save(file);
            pdfSniper.splitPdf(file);
            return ResponseEntity.status(HttpStatus.OK)
                .body("Uploaded the file successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            logger.error("Failed to upload file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Upload failed: " + e.getMessage());
        }
    }
}
