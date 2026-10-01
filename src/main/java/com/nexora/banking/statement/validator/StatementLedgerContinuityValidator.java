package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.StatementIntegrityException;
import com.nexora.banking.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StatementLedgerContinuityValidator {

    public void validate(List<Transaction> transactions) {

        if (transactions == null || transactions.size() < 2) {
            return;
        }

        for (int i = 1; i < transactions.size(); i++) {

            Transaction previousTransaction =
                    transactions.get(i - 1);

            Transaction currentTransaction =
                    transactions.get(i);

            BigDecimal previousBalanceAfter =
                    previousTransaction.getBalanceAfter();

            BigDecimal currentBalanceBefore =
                    currentTransaction.getBalanceBefore();

            if (
                    previousBalanceAfter.compareTo(
                            currentBalanceBefore
                    ) != 0
            ) {
                throw new StatementIntegrityException(
                        "Statement transaction balances are not continuous."
                );
            }
        }
    }
}