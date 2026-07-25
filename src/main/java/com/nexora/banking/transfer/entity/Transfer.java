package com.nexora.banking.transfer.entity;

import com.nexora.banking.common.entity.BaseEntity;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.transfer.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "transfers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transfer_idempotency_key",
                        columnNames = "idempotency_key"
                ),
                @UniqueConstraint(
                        name = "uk_transfer_reference",
                        columnNames = "reference"
                )
        },
        indexes = {
                @Index(name = "idx_transfer_sender", columnList = "sender_id"),
                @Index(name = "idx_transfer_receiver", columnList = "receiver_id"),
                @Index(name = "idx_transfer_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sender_id",
            nullable = false
    )
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "receiver_id",
            nullable = false
    )
    private User receiver;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String idempotencyKey;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String reference;

    @Column(name = "completed_at")
    private Instant completedAt;

}