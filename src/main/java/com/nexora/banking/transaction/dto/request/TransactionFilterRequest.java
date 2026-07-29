package com.nexora.banking.transaction.dto.request;

import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionFilterRequest(

        TransactionType type,

        TransactionCategory category,

        TransactionStatus status,

        Instant from,

        Instant to,

        BigDecimal minAmount,

        BigDecimal maxAmount

) {
}