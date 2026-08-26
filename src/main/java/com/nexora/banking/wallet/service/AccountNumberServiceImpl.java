package com.nexora.banking.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountNumberServiceImpl
        implements AccountNumberService {

    private static final String BANK_CODE = "999";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String generateAccountNumber() {

        Long sequenceValue = jdbcTemplate.queryForObject(
                "SELECT nextval('account_number_sequence')",
                Long.class
        );

        if (sequenceValue == null) {
            throw new IllegalStateException(
                    "Failed to generate account number sequence."
            );
        }

        String sequencePart = String.format(
                "%07d",
                sequenceValue
        );

        String accountNumberWithoutCheckDigit =
                BANK_CODE + sequencePart;

        int checkDigit = calculateCheckDigit(
                accountNumberWithoutCheckDigit
        );

        return accountNumberWithoutCheckDigit + checkDigit;
    }

    private int calculateCheckDigit(String value) {

        int sum = 0;

        for (char digit : value.toCharArray()) {
            sum += Character.getNumericValue(digit);
        }

        return sum % 10;
    }
}