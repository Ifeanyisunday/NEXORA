package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatementOpeningBalanceValidatorTest {

    private final StatementOpeningBalanceValidator validator =
            new StatementOpeningBalanceValidator();

    @Test
    void shouldPassWhenOpeningBalanceMatchesFirstTransaction() {

        Transaction firstTransaction = Transaction.builder()
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        assertDoesNotThrow(() ->
                validator.validate(
                        new BigDecimal("50000.00"),
                        firstTransaction
                )
        );
    }

    @Test
    void shouldFailWhenOpeningBalanceDoesNotMatchFirstTransaction() {

        Transaction firstTransaction = Transaction.builder()
                .balanceBefore(new BigDecimal("45000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(
                        new BigDecimal("50000.00"),
                        firstTransaction
                )
        );
    }

    @Test
    void shouldPassWhenThereAreNoTransactions() {

        assertDoesNotThrow(() ->
                validator.validate(
                        new BigDecimal("50000.00"),
                        null
                )
        );
    }

    @Test
    void shouldFailWhenOpeningBalanceIsNull() {

        Transaction firstTransaction = Transaction.builder()
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(
                        null,
                        firstTransaction
                )
        );
    }
}