package com.nexora.banking.notification.repository;

import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    long countByUserAndReadFalse(User user);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}