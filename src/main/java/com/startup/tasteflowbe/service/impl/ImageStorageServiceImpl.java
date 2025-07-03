package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.ImageStorage;
import com.startup.tasteflowbe.repository.ImageStorageRepository;
import com.startup.tasteflowbe.service.ImageStorageService;
import com.startup.tasteflowbe.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private final S3Service s3Service;
    private final ImageStorageRepository imageRepo;

    @Override
    public ImageStorage uploadImage(MultipartFile file, String relatedTable, Long recordId) throws IOException {
        String imageUrl = s3Service.uploadImage(file);
        ImageStorage image = ImageStorage.builder()
                .imageUrl(imageUrl)
                .referenceTable(relatedTable)
                .referenceId(recordId)
                .description("Ảnh được upload từ FE")
                .createdAt(LocalDateTime.now())
                .build();
        return imageRepo.save(image);
    }

    @Override
    public Optional<ImageStorage> getFirstImage(String relatedTable, Long recordId) {
        return imageRepo.findFirstByReferenceTableAndReferenceId(relatedTable, recordId);
    }

    @Override
    public List<ImageStorage> getAllImages(String relatedTable, Long recordId) {
        return imageRepo.findAllByReferenceTableAndReferenceId(relatedTable, recordId);
    }

    @Override
    public void deleteImages(String relatedTable, Long recordId) {
        List<ImageStorage> images = imageRepo.findAllByReferenceTableAndReferenceId(relatedTable, recordId);
        imageRepo.deleteAll(images);
    }

    @Override
    public void deleteImageById(Long imageId) {
        imageRepo.deleteById(imageId);
    }
}