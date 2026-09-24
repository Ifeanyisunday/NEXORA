package com.nexora.banking.statement.validator;

import org.springframework.stereotype.Component;
import com.nexora.banking.statement.exception.StatementIntegrityException;

import java.math.BigDecimal;

@Component
public class StatementIntegrityValidator {

    public void validate(
            BigDecimal openingBalance,
            BigDecimal totalCredits,
            BigDecimal totalDebits,
            BigDecimal closingBalance
    ) {

        BigDecimal expectedClosingBalance =
                openingBalance
                        .add(totalCredits)
                        .subtract(totalDebits);

        if (
                expectedClosingBalance.compareTo(
                        closingBalance
                ) != 0
        ) {

            throw new StatementIntegrityException(
                    "Statement balances do not reconcile."
            );
        }
    }
}