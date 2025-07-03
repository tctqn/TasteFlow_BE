// package com.startup.tasteflowbe.controller;

// import com.startup.tasteflowbe.service.S3Service;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.nio.file.Paths;
// import java.util.UUID;

// @RestController
// public class S3Controller {

// @Autowired
// private S3Service s3Service;

// // Kiểm tra kết nối đến S3
// @GetMapping("/test-s3-connection")
// public String testS3Connection() {
// s3Service.testConnection(); // Kiểm tra kết nối S3
// return "Check the logs for connection results!";
// }

// // Kiểm tra kết nối tới bucket
// @GetMapping("/test-bucket-connection")
// public String testBucketConnection() {
// s3Service.testConnectionToBucket("tasteflow"); // Thay "tasteflow" bằng tên
// bucket của bạn
// return "Check the logs for bucket connection result!";
// }

// // Liệt kê các objects trong một bucket
// @GetMapping("/list-bucket-objects")
// public String listBucketObjects() {
// s3Service.listObjectsInBucket("tasteflow"); // Thay "tasteflow" bằng tên
// bucket của bạn
// return "Check the logs for objects in the bucket!";
// }

// @GetMapping("/upload-file")
// public String uploadFile() {
// // Cập nhật tên bucket
// String bucketName = "tasteflow";

// // Đường dẫn tới các file cần upload
// String filePath1 = "/home/tctqn/Downloads/food1.jpeg";
// String filePath2 = "/home/tctqn/Downloads/food2.jpg";
// String filePath3 = "/home/tctqn/Downloads/food3.jpg";

// // Tạo key duy nhất cho mỗi file
// String key1 = generateUniqueKey(filePath1);
// String key2 = generateUniqueKey(filePath2);
// String key3 = generateUniqueKey(filePath3);

// // Upload file lên S3
// s3Service.uploadFile(bucketName, key1, filePath1);
// s3Service.uploadFile(bucketName, key2, filePath2);
// s3Service.uploadFile(bucketName, key3, filePath3);

// return "Check the logs for file upload result!";
// }

// // Hàm tạo key duy nhất cho file (UUID + tên gốc)
// private String generateUniqueKey(String filePath) {
// String fileName = Paths.get(filePath).getFileName().toString(); // Lấy tên
// file gốc
// String uniqueId = UUID.randomUUID().toString(); // Tạo UUID duy nhất
// return uniqueId + "_" + fileName; // Kết hợp UUID và tên file
// }

// // Download một file từ S3
// @GetMapping("/download-file")
// public String downloadFile() {
// // Cập nhật tên file và đường dẫn bạn muốn lưu file
// String bucketName = "tasteflow";
// String key = "test-file.txt"; // Key của file trên S3
// String downloadPath = "path/to/local/downloaded-file.txt"; // Đường dẫn lưu
// file đã tải về

// s3Service.downloadFile(bucketName, key, downloadPath);
// return "Check the logs for file download result!";
// }
// }
