package com.nexora.banking.wallet.entity;

import com.nexora.banking.common.entity.BaseEntity;
import com.nexora.banking.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;



    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);

    }


    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient balance."
            );
        }
        this.balance = this.balance.subtract(amount);
    }


    public void freeze() {
        this.status = WalletStatus.FROZEN;
    }
    

    public void activate() {
        this.status = WalletStatus.ACTIVE;
    }

    public void close() {
        this.status = WalletStatus.CLOSED;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero."
            );
        }
    }
}