package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionBalanceValidator {

    public void validate(Transaction transaction) {

        if (transaction == null) {
            throw new StatementIntegrityException(
                    "Transaction cannot be null."
            );
        }

        BigDecimal balanceBefore = transaction.getBalanceBefore();
        BigDecimal amount = transaction.getAmount();
        BigDecimal balanceAfter = transaction.getBalanceAfter();

        if (balanceBefore == null
                || amount == null
                || balanceAfter == null) {

            throw new StatementIntegrityException(
                    "Transaction balance values cannot be null."
            );
        }

        TransactionType type = transaction.getType();

        if (type == null) {
            throw new StatementIntegrityException(
                    "Transaction type cannot be null."
            );
        }

        BigDecimal expectedBalanceAfter;

        if (type == TransactionType.CREDIT) {

            expectedBalanceAfter =
                    balanceBefore.add(amount);

        } else if (type == TransactionType.DEBIT) {

            expectedBalanceAfter =
                    balanceBefore.subtract(amount);

        } else {

            throw new StatementIntegrityException(
                    "Unsupported transaction type."
            );
        }

        if (expectedBalanceAfter.compareTo(balanceAfter) != 0) {

            throw new StatementIntegrityException(
                    "Transaction balance does not match transaction amount."
            );
        }
    }
}