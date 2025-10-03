package com.fcj.problem_suggest.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    void save(MultipartFile file) throws IOException;
    void saveBytes(byte[] data, String fileName) throws IOException;
    void delete(String fileName) throws IOException;
}