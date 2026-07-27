package com.nexora.banking.common.exception;

public class IdempotencyKeyConflictException
        extends RuntimeException {

    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}