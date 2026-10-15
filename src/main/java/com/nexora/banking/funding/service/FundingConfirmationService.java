package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.enums.FundingProvider;

public interface FundingConfirmationService {

    FundingResponse confirmFunding(
            String fundingReference,
            FundingProvider provider,
            String providerReference,
            boolean successful
    );
}