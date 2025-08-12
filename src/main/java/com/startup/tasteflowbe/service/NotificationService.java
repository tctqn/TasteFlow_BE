package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.NotificationDTO;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.model.UserNotification;

import java.util.List;

public interface NotificationService {
    void sendNotificationToUsers(List<Long> userIds, NotificationType type, String content);
    List<NotificationDTO> getNotificationsForUser(Long userId);
    void markAsRead(Long userId, Long notificationId);
}
