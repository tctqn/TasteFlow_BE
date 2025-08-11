package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.UserNotification;
import com.startup.tasteflowbe.model.UserNotificationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationKey> {
    @Query("""
    SELECT un 
    FROM UserNotification un
    JOIN FETCH un.notification n
    WHERE un.user.id = :userId
    ORDER BY n.createdAt DESC
""")
List<UserNotification> findByUserIdOrderByNotificationCreatedAtDesc(@Param("userId") Long userId);
}
