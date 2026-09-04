package com.nexora.banking.notification.entity;

import com.nexora.banking.notification.enums.NotificationType;
import com.nexora.banking.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notification_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_notification_transfer",
                        columnList = "transfer_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_transfer_user_type",
                        columnNames = {
                                "transfer_id",
                                "user_id",
                                "type"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "transfer_id",
            nullable = false
    )
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private NotificationType type;

    @Column(
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            nullable = false,
            length = 1000
    )
    private String message;

    @Column(
            nullable = false
    )
    @Builder.Default
    private boolean read = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}