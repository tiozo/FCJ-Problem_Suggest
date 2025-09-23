package com.fcj.problem_suggest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void save(MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(file.getOriginalFilename())
            .contentType(file.getContentType())
            .build();
            
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }
}