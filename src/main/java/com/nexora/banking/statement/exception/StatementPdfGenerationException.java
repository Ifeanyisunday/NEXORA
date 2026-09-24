package com.nexora.banking.statement.exception;

public class StatementPdfGenerationException
        extends RuntimeException {

    public StatementPdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
