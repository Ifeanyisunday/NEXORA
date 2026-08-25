package com.nexora.banking.notification.service;

import com.nexora.banking.common.exception.ResourceNotFoundException;
import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.notification.enums.NotificationType;
import com.nexora.banking.notification.repository.NotificationRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification create(
            User user,
            NotificationType type,
            String title,
            String message
    ) {

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }
}