package com.zyna.dev.ecommerce.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NotificationPayload {
    private NotificationType type;
    private String subject;
    private String body;
}
