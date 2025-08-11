package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.response.NotificationDTO;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.model.UserNotification;
import com.startup.tasteflowbe.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotificationToUsers(request.getUserIds(), request.getType(), request.getContent());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotificationsForUser(userId);
    }

    @PutMapping("/{userId}/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long userId, @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class NotificationRequest {
        private List<Long> userIds;
        private NotificationType type;
        private String content;
    }
}
