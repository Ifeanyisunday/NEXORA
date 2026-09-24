package com.nexora.banking.statement.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StatementResponse(

        String statementReference,

        Instant generatedAt,

        LocalDate from,

        LocalDate to,

        BigDecimal openingBalance,

        BigDecimal closingBalance,

        BigDecimal totalCredits,

        BigDecimal totalDebits,

        List<StatementItemResponse> transactions

) {
}