package com.nexora.banking.notification.service;

import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.notification.enums.NotificationType;
import com.nexora.banking.transfer.event.TransferCompletedEvent;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void createTransferNotifications(
            TransferCompletedEvent event
    );

    Page<NotificationResponse> getUserNotifications(
        UUID userId,
        Pageable pageable
    );

    void markAsRead(
        UUID userId,
        UUID notificationId
    );
}