package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.response.NotificationDTO;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.exception.ResourceNotFoundException;
import com.startup.tasteflowbe.model.Notification;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserNotification;
import com.startup.tasteflowbe.model.UserNotificationKey;
import com.startup.tasteflowbe.repository.NotificationRepository;
import com.startup.tasteflowbe.repository.UserNotificationRepository;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    @Override
    public void sendNotificationToUsers(List<Long> userIds, NotificationType type, String content) {
        // Tạo thông báo
        Notification notification = new Notification();
        notification.setType(type);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        notificationRepository.save(notification);

        // Gắn thông báo vào từng user
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            UserNotification userNotification = new UserNotification();
            userNotification.setId(new UserNotificationKey(user.getUserId(), notification.getId()));
            userNotification.setUser(user);
            userNotification.setNotification(notification);
            userNotification.setIsRead(false);
            userNotificationRepository.save(userNotification);
        }
    }

    @Override
    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        return userNotificationRepository
                .findByUserIdOrderByNotificationCreatedAtDesc(userId)
                .stream()
                .map(un -> new NotificationDTO(
                        un.getUser().getUserId(),                // user_id
                        un.getNotification().getId(),            // notification_id
                        un.getIsRead(),                        // is_read
                        un.getNotification().getType(),          // type
                        un.getNotification().getContent(),       // content
                        un.getNotification().getCreatedAt()      // created_at
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        UserNotificationKey key = new UserNotificationKey(userId, notificationId);
        UserNotification userNotification = userNotificationRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found for this user"));

        userNotification.setIsRead(true);
        userNotificationRepository.save(userNotification);
    }
}
