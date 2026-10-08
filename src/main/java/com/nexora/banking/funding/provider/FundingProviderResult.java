package com.nexora.banking.funding.provider;

public record FundingProviderResult(
        boolean successful,
        String providerReference
) {
}