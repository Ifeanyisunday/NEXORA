package com.nexora.banking.common.exception;

public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException(String email){
        super("Email already exists: " + email);
    }
    // Email already exists or Wrong password
}