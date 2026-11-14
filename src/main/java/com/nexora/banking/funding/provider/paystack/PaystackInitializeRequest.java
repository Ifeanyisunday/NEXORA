package com.nexora.banking.funding.provider.paystack;

public record PaystackInitializeRequest(
        String email,
        long amount,
        String currency,
        String reference
) {
}
