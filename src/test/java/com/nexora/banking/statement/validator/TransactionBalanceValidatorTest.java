package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionBalanceValidatorTest {

    private final TransactionBalanceValidator validator =
            new TransactionBalanceValidator();

    @Test
    void shouldPassForValidCreditTransaction() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("10000.00"))
                .balanceBefore(new BigDecimal("40000.00"))
                .balanceAfter(new BigDecimal("50000.00"))
                .build();

        assertDoesNotThrow(() ->
                validator.validate(transaction)
        );
    }

    @Test
    void shouldPassForValidDebitTransaction() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("10000.00"))
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        assertDoesNotThrow(() ->
                validator.validate(transaction)
        );
    }

    @Test
    void shouldFailForInvalidCreditBalance() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("10000.00"))
                .balanceBefore(new BigDecimal("40000.00"))
                .balanceAfter(new BigDecimal("45000.00"))
                .build();

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(transaction)
        );
    }

    @Test
    void shouldFailForInvalidDebitBalance() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("10000.00"))
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("45000.00"))
                .build();

        assertThrows(
                StatementIntegrityException.class,
                () -> validator.validate(transaction)
        );
    }
}