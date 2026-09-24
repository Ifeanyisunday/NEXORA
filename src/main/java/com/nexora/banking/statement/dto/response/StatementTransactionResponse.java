package com.nexora.banking.statement.dto.response;

import java.math.BigDecimal;
// import java.time.Instant;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;


public record StatementTransactionResponse(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate transactionDate,

        String description,

        String reference,

        BigDecimal debit,

        BigDecimal credit,

        BigDecimal balance

) {}
