package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
