package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;

public interface FundingProviderClient {

    FundingProvider provider();

    FundingProviderResult initiate(Funding funding);
}