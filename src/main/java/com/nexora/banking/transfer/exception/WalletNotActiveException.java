package com.nexora.banking.transfer.exception;

public class WalletNotActiveException extends RuntimeException {

    public WalletNotActiveException(String message) {
        super(message);
    }
}