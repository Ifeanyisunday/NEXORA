package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StatementOpeningBalanceValidator {

    public void validate(
            BigDecimal openingBalance,
            Transaction firstTransaction
    ) {

        if (firstTransaction == null) {
            return;
        }

        if (openingBalance == null) {
            throw new StatementIntegrityException(
                    "Statement opening balance cannot be null."
            );
        }

        BigDecimal firstTransactionBalanceBefore =
                firstTransaction.getBalanceBefore();

        if (firstTransactionBalanceBefore == null) {
            throw new StatementIntegrityException(
                    "First transaction balance before cannot be null."
            );
        }

        if (openingBalance.compareTo(
                firstTransactionBalanceBefore
        ) != 0) {

            throw new StatementIntegrityException(
                    "Statement opening balance does not match the first transaction balance."
            );
        }
    }
}