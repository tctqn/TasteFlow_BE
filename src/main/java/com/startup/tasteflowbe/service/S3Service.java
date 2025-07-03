package com.startup.tasteflowbe.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String upload(MultipartFile file);
    void testConnection();
    void testConnectionToBucket(String bucketName);
    void listObjectsInBucket(String bucketName);
    String uploadInvoice(String orderCode, byte[] pdfBytes);
    void uploadFile(String bucketName, String key, String filePath);
    void downloadFile(String bucketName, String key, String downloadPath);
    String generatePresignedUrl(String key);
}