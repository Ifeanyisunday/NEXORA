package com.nexora.banking.statement.exception;

public class InvalidStatementDateException
        extends RuntimeException {

    public InvalidStatementDateException(String message) {
        super(message);
    }
}
