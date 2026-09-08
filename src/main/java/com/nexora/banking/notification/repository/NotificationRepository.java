package com.nexora.banking.notification.repository;

import com.nexora.banking.notification.entity.Notification;
import com.nexora.banking.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    @Modifying 
    @Query(""" 
        UPDATE Notification n 
        SET n.read = true 
        WHERE n.id = :notificationId 
        AND n.user.id = :userId 
        """) 
    int markAsRead( 
        @Param("notificationId") UUID notificationId, 
        @Param("userId") UUID userId 
    );
}