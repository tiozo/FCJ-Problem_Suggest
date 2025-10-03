package com.fcj.problem_suggest.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
    private final Path uploadPath = Paths.get("uploads");

    public LocalStorageService() throws IOException {
        Files.createDirectories(uploadPath);
    }

    @Override
    public void save(MultipartFile file) throws IOException {
        Path targetPath = uploadPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void saveBytes(byte[] data, String fileName) throws IOException {
        Path targetPath = uploadPath.resolve(fileName);
        Files.write(targetPath, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        Path targetPath = uploadPath.resolve(fileName);
        Files.deleteIfExists(targetPath);
    }
}