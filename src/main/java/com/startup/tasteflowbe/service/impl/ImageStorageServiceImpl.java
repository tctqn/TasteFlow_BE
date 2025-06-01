package com.startup.tasteflowbe.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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

@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private final AmazonS3 amazonS3;
    private final ImageStorageRepository imageRepo;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public ImageStorage uploadImage(MultipartFile file, String relatedTable, Long recordId) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(bucket, key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        String fileUrl = amazonS3.getUrl(bucket, key).toString();

        ImageStorage image = ImageStorage.builder()
                .imageUrl(fileUrl)
                .relatedTable(relatedTable)
                .recordId(recordId)
                .uploadedAt(LocalDateTime.now())
                .build();

        return imageRepo.save(image);
    }
}
