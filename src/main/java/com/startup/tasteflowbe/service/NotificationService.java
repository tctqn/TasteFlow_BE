package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getAllNotifications();
    List<Notification> getUnreadNotifications();
    Notification markAsRead(Long id);
    void deleteNotification(Long id);
}
