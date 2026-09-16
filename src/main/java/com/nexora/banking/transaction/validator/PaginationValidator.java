package com.nexora.banking.transaction.validator;

import com.nexora.banking.transaction.exception.InvalidPaginationException;
import org.springframework.stereotype.Component;

@Component
public class PaginationValidator {

    private static final int MAX_PAGE_SIZE = 100;

    public void validate(int page, int size) {

        if (page < 0) {
            throw new InvalidPaginationException(
                    "Page cannot be negative."
            );
        }

        if (size < 1) {
            throw new InvalidPaginationException(
                    "Page size must be at least 1."
            );
        }

        if (size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "Page size cannot exceed "
                            + MAX_PAGE_SIZE
                            + "."
            );
        }
    }
}