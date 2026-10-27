package com.nexora.banking.transaction.entity;

import com.nexora.banking.common.entity.BaseEntity;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_reference",
                        columnNames = "reference"
                ),
                @UniqueConstraint(
                        name = "uk_transaction_funding_id",
                        columnNames = "funding_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_transaction_wallet",
                        columnList = "wallet_id"
                ),
                @Index(
                        name = "idx_transaction_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_transaction_wallet_created_id",
                        columnList = "wallet_id, created_at, id"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wallet_id",
            nullable = false
    )
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory category;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "balance_before",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balanceBefore;

    @Column(
            name = "balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balanceAfter;

    @Column(
            nullable = false,
            length = 100
    )
    private String reference;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    /*
     * Most transactions are not wallet-funding entries, so this field
     * remains nullable. The database unique constraint allows multiple
     * NULL values but only one row per non-null funding ID.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id")
    private Funding funding;
}