package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.enums.NotificationType;
import lombok.Data;

import java.util.List;

@Data
public class NotificationRequestDTO {
    private List<Long> userIds;
    private NotificationType type;
    private String content;
}
