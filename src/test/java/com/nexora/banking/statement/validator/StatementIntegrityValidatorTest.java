package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StatementIntegrityValidatorTest {

    private final StatementIntegrityValidator validator =
            new StatementIntegrityValidator();

    @Test
    void shouldPassWhenStatementBalancesReconcile() {

        BigDecimal openingBalance =
                new BigDecimal("50000.00");

        BigDecimal totalCredits =
                new BigDecimal("10000.00");

        BigDecimal totalDebits =
                new BigDecimal("5000.00");

        BigDecimal closingBalance =
                new BigDecimal("55000.00");

        assertDoesNotThrow(() ->
                validator.validate(
                        openingBalance,
                        totalCredits,
                        totalDebits,
                        closingBalance
                )
        );
    }

    @Test
    void shouldFailWhenStatementBalancesDoNotReconcile() {

        BigDecimal openingBalance =
                new BigDecimal("50000.00");

        BigDecimal totalCredits =
                new BigDecimal("10000.00");

        BigDecimal totalDebits =
                new BigDecimal("5000.00");

        BigDecimal closingBalance =
                new BigDecimal("52000.00");

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(
                        openingBalance,
                        totalCredits,
                        totalDebits,
                        closingBalance
                )
        );
    }
}