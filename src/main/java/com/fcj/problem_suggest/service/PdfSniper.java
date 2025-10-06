package com.fcj.problem_suggest.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PdfSniper {
    
    private final StorageService storageService;

    public void splitPdf(MultipartFile file) throws IOException {
        if (!"application/pdf".equals(file.getContentType())) {
            return;
        }

        try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
            int totalPages = document.getNumberOfPages();
            
            if (totalPages < 8) {
                return;
            }

            String originalName = file.getOriginalFilename();
            String baseName = Objects.requireNonNull(originalName).substring(0, originalName.lastIndexOf('.'));
            
            List<PDDocument> splitDocs = new ArrayList<>();
            
            for (int i = 0; i < totalPages; i += 8) {
                PDDocument splitDoc = new PDDocument();
                int endPage = Math.min(i + 8, totalPages);
                
                for (int j = i; j < endPage; j++) {
                    splitDoc.addPage(document.getPage(j));
                }
                
                splitDocs.add(splitDoc);
            }
            
            for (int i = 0; i < splitDocs.size(); i++) {
                String fileName = baseName + "_file" + (i + 1) + ".pdf";
                
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    splitDocs.get(i).save(baos);
                    storageService.saveBytes(baos.toByteArray(), fileName);
                }
                
                splitDocs.get(i).close();
            }
            
            storageService.delete(originalName);
        }
    }
}