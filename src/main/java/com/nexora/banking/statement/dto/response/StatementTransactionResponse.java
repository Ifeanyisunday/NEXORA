package com.nexora.banking.statement.dto.response;

import java.math.BigDecimal;
import java.time.Instant;


public record StatementTransactionResponse(

        Instant transactionDate,

        String description,

        String reference,

        BigDecimal debit,

        BigDecimal credit,

        BigDecimal balance

) {}
