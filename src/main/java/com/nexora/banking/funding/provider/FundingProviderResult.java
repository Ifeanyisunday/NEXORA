package com.nexora.banking.funding.provider;

public record FundingProviderResult(
        boolean successful,
        String providerReference,
        String authorizationUrl
) {

    /*
     * Backward-compatible constructor for MOCK and
     * existing tests that do not provide a checkout URL.
     */
    public FundingProviderResult(
            boolean successful,
            String providerReference
    ) {
        this(successful, providerReference, null);
    }
}
