package com.nexora.banking.funding.exception;

public class FundingIdempotencyKeyConflictException extends RuntimeException {

    public FundingIdempotencyKeyConflictException(String message) {
        super(message);
    }
}