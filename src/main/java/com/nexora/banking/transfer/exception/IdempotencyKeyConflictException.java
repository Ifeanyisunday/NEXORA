package com.nexora.banking.transfer.exception;

public class IdempotencyKeyConflictException
        extends RuntimeException {

    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}
