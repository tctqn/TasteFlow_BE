package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
@Data
@NoArgsConstructor
public class UserNotification {

    @EmbeddedId
    private UserNotificationKey id = new UserNotificationKey();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("notificationId")
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

}
