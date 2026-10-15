package com.nexora.banking.funding.provider;

public record MockFundingConfirmation(
        String fundingReference,
        String providerReference,
        boolean successful
) {}