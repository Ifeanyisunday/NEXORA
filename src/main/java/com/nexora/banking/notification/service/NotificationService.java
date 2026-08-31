package com.nexora.banking.notification.service;

import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.notification.enums.NotificationType;
import com.nexora.banking.transfer.event.TransferCompletedEvent;
import com.nexora.banking.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createTransferNotifications(
            TransferCompletedEvent event
    );

    // List<Notification> getMyNotifications(UUID userId);
}