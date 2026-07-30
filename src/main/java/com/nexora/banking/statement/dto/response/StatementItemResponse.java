package com.nexora.banking.statement.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record StatementItemResponse(

        Instant date,

        String reference,

        String description,

        BigDecimal debit,

        BigDecimal credit,

        BigDecimal balance

) {
}