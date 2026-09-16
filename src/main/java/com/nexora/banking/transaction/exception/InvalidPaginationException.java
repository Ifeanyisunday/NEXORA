package com.nexora.banking.transaction.exception;

public class InvalidPaginationException
        extends RuntimeException {

    public InvalidPaginationException(String message) {
        super(message);
    }
}