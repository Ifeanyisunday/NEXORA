package com.nexora.banking.statement.validator;

import com.nexora.banking.statement.exception.InvalidStatementDateException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StatementDateValidator {

    public void validate(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null || to == null) {
            throw new InvalidStatementDateException(
                    "Statement start date and end date are required."
            );
        }

        if (from.isAfter(to)) {
            throw new InvalidStatementDateException(
                    "Statement start date cannot be after end date."
            );
        }
    }
}