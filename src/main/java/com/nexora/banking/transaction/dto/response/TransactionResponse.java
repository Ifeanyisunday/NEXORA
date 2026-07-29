package com.nexora.banking.transaction.dto.response;

import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(

        UUID id,

        TransactionType type,

        TransactionCategory category,

        BigDecimal amount,

        BigDecimal balanceBefore,

        BigDecimal balanceAfter,

        String reference,

        String description,

        TransactionStatus status,

        Instant createdAt

) {
}