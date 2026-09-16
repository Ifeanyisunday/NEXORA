package com.nexora.banking.transaction.validator;

import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.exception.InvalidTransactionFilterException;
import org.springframework.stereotype.Component;

@Component
public class TransactionFilterValidator {

    public void validate(TransactionFilterRequest filter) {

        if (filter == null) {
            return;
        }

        validateAmountRange(filter);
        validateDateRange(filter);
    }

    private void validateAmountRange(
            TransactionFilterRequest filter
    ) {
        if (filter.minAmount() != null
                && filter.maxAmount() != null
                && filter.minAmount()
                    .compareTo(filter.maxAmount()) > 0) {

            throw new InvalidTransactionFilterException(
                    "Minimum amount cannot be greater than maximum amount."
            );
        }
    }

    private void validateDateRange(
            TransactionFilterRequest filter
    ) {
        if (filter.from() != null
                && filter.to() != null
                && filter.from().isAfter(filter.to())) {

            throw new InvalidTransactionFilterException(
                    "From date cannot be after to date."
            );
        }
    }
}