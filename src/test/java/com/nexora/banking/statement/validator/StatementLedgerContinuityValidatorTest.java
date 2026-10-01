package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatementLedgerContinuityValidatorTest {

    private final StatementLedgerContinuityValidator validator =
            new StatementLedgerContinuityValidator();

    @Test
    void shouldPassWhenTransactionBalancesAreContinuous() {

        Transaction first = Transaction.builder()
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        Transaction second = Transaction.builder()
                .balanceBefore(new BigDecimal("40000.00"))
                .balanceAfter(new BigDecimal("45000.00"))
                .build();

        assertDoesNotThrow(() ->
                validator.validate(List.of(first, second))
        );
    }

    @Test
    void shouldFailWhenTransactionBalancesAreNotContinuous() {

        Transaction first = Transaction.builder()
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        Transaction second = Transaction.builder()
                .balanceBefore(new BigDecimal("35000.00"))
                .balanceAfter(new BigDecimal("45000.00"))
                .build();

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(List.of(first, second))
        );
    }

    @Test
    void shouldPassWhenThereAreFewerThanTwoTransactions() {

        Transaction transaction = Transaction.builder()
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        assertDoesNotThrow(() ->
                validator.validate(List.of(transaction))
        );
    }
}