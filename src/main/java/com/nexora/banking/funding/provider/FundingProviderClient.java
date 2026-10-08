package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.provider.FundingProviderResult;

public interface FundingProviderClient {

    FundingProviderResult initiate(Funding funding);
}