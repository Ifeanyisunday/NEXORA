package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.service.FundingConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockFundingConfirmationService {

    private final FundingConfirmationService fundingConfirmationService;

    public FundingResponse handle(
            MockFundingConfirmation confirmation
    ) {
        return fundingConfirmationService.confirmFunding(
                confirmation.fundingReference(),
                FundingProvider.MOCK,
                confirmation.providerReference(),
                confirmation.successful()
        );
    }
}