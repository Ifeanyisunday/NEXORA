package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.entity.Funding;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockFundingProvider implements FundingProviderClient {

    @Override
    public FundingProviderResult initiate(Funding funding) {

        String providerReference =
                "MOCK-" + UUID.randomUUID();

        return new FundingProviderResult(
                true,
                providerReference
        );
    }
}