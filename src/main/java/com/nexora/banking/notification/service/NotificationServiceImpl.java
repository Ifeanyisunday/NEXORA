package com.nexora.banking.notification.service;

import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.notification.enums.NotificationType;
import com.nexora.banking.notification.repository.NotificationRepository;
import com.nexora.banking.transfer.event.TransferCompletedEvent;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTransferNotifications(
            TransferCompletedEvent event
    ) {

        log.info(
                "Creating notifications for transferId={}",
                event.transferId()
        );

        User sender = userRepository
                .findById(event.senderId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Sender not found."
                        )
                );

        User receiver = userRepository
                .findById(event.receiverId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Receiver not found."
                        )
                );

        log.info(
                "Sender and receiver found. sender={}, receiver={}",
                sender.getUsername(),
                receiver.getUsername()
        );

        Notification senderNotification =
                Notification.builder()
                        .user(sender)
                        .type(NotificationType.TRANSFER_SENT)
                        .title("Transfer Successful")
                        .message(
                                "You successfully transferred "
                                        + event.amount()
                                        + " to "
                                        + receiver.getUsername()
                        )
                        .build();

        Notification receiverNotification =
                Notification.builder()
                        .user(receiver)
                        .type(NotificationType.TRANSFER_RECEIVED)
                        .title("Money Received")
                        .message(
                                "You received "
                                        + event.amount()
                                        + " from "
                                        + sender.getUsername()
                        )
                        .build();
        log.info("Saving sender notification");

        notificationRepository.saveAndFlush(
                senderNotification
        );

        log.info("Saving receiver notification");

        notificationRepository.saveAndFlush(
                receiverNotification
        );

        log.info(
                "Notifications created successfully for transferId={}",
                event.transferId()
        );
    }
}