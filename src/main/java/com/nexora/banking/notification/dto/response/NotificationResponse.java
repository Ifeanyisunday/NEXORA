package com.nexora.banking.notification.dto.response;

import com.nexora.banking.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID transferId,
        NotificationType type,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
}