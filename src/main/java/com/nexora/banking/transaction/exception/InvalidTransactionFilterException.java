package com.nexora.banking.transaction.exception;

public class InvalidTransactionFilterException
        extends RuntimeException {

    public InvalidTransactionFilterException(String message) {
        super(message);
    }
}