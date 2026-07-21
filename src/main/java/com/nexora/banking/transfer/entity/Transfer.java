package com.nexora.banking.transfer.entity;

import com.nexora.banking.common.entity.BaseEntity;
import com.nexora.banking.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "transfers",
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

}