package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

        private S3Presigner presigner;

        @Value("${cloud.aws.credentials.access-key}")
        private String accessKey;

        @Value("${cloud.aws.credentials.secret-key}")
        private String secretKey;

        @Value("${cloud.aws.region.static}")
        private String region;

        @Value("${cloud.aws.s3.bucket}")
        private String bucket;

        private S3Client s3Client;

        @PostConstruct
        public void init() {
                s3Client = S3Client.builder()
                                .region(Region.of(region))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .build();

                presigner = S3Presigner.builder()
                                .region(Region.of(region))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .build();
        }

        @Override
        public String uploadInvoice(String orderCode, byte[] pdfBytes) {
                String fileName = "invoices/invoice_" + orderCode + ".pdf";

                PutObjectRequest putRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(fileName)
                                .contentType("application/pdf")
                                .build();

                s3Client.putObject(putRequest, RequestBody.fromBytes(pdfBytes));

                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
        }

        @Override
        public String uploadImage(MultipartFile file) {
                try {
                        String key = "images/"
                                        + LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                                        + "_" + file.getOriginalFilename();

                        PutObjectRequest putRequest = PutObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(key)
                                        .contentType(file.getContentType())
                                        .build();

                        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
                        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

                } catch (IOException e) {
                        throw new RuntimeException("Failed to upload to S3", e);
                }
        }

        @Override
        public String generatePresignedUrl(String key) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                .getObjectRequest(getObjectRequest)
                                .signatureDuration(Duration.ofMinutes(1))
                                .build();

                return presigner.presignGetObject(presignRequest)
                                .url()
                                .toString();
        }

        public String uploadImageFromBytes(byte[] imageBytes, String fileName) {
                try {
                        String key = "images/"
                                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                                        + "_" + fileName;

                        PutObjectRequest putRequest = PutObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(key)
                                        .contentType("image/png")
                                        .build();

                        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
                        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to upload QR code to S3", e);
                }
        }

}