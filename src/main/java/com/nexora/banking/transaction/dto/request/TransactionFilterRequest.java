package com.nexora.banking.transaction.dto.request;

import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionFilterRequest(

        TransactionType type,

        TransactionCategory category,

        TransactionStatus status,

        LocalDate from,

        LocalDate to,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Minimum amount cannot be negative."
        )
        BigDecimal minAmount,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Maximum amount cannot be negative."
        )
        BigDecimal maxAmount

) {}