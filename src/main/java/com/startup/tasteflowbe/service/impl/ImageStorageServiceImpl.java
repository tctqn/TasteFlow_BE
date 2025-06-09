package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.ImageStorage;
import com.startup.tasteflowbe.repository.ImageStorageRepository;
import com.startup.tasteflowbe.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private final S3Client s3Client;
    private final ImageStorageRepository imageRepo;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public ImageStorage uploadImage(MultipartFile file, String relatedTable, Long recordId) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + key;

        ImageStorage image = ImageStorage.builder()
                .imageUrl(fileUrl)
                .referenceTable(relatedTable)
                .referenceId(recordId)
                .createdAt(LocalDateTime.now())
                .build();

        return imageRepo.save(image);
    }
}