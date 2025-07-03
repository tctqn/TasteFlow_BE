package com.startup.tasteflowbe.service;

import org.springframework.web.multipart.MultipartFile;
import com.startup.tasteflowbe.model.ImageStorage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ImageStorageService {
    ImageStorage uploadImage(MultipartFile file, String relatedTable, Long recordId) throws IOException;

    Optional<ImageStorage> getFirstImage(String relatedTable, Long recordId);

    List<ImageStorage> getAllImages(String relatedTable, Long recordId);

    void deleteImages(String relatedTable, Long recordId);

    void deleteImageById(Long imageId);
}
