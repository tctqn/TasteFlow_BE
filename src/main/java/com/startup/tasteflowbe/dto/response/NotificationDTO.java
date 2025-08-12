package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.NotificationType;
import lombok.AllArgsConstructor; // Thêm annotation này
import lombok.Data;
import lombok.NoArgsConstructor; // (Tùy chọn) Để giữ lại constructor không tham số

import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Thêm annotation này để Lombok tạo constructor không tham số
@AllArgsConstructor // Thêm annotation này để Lombok tạo constructor đầy đủ tham số
public class NotificationDTO {
    Long userId;
    Long notificationId;
    Boolean isRead;
    NotificationType type;
    String content;
    LocalDateTime createdAt;
}