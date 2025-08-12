package com.startup.tasteflowbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationKey implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "notification_id")
    private Long notificationId;
}
