package com.startup.tasteflowbe.service;

import org.springframework.web.multipart.MultipartFile;
import com.startup.tasteflowbe.model.ImageStorage;

import java.io.IOException;

public interface ImageStorageService {
    ImageStorage uploadImage(MultipartFile file, String relatedTable, Long recordId) throws IOException;
}
