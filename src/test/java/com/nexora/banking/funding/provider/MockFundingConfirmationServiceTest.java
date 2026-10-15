package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.service.FundingConfirmationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockFundingConfirmationServiceTest {

    @Mock
    private FundingConfirmationService fundingConfirmationService;

    @InjectMocks
    private MockFundingConfirmationService mockFundingConfirmationService;

    @Test
    void handle_shouldDelegateSuccessfulConfirmation() {
        String fundingReference = "FND-" + UUID.randomUUID();
        String providerReference = "MOCK-" + UUID.randomUUID();

        MockFundingConfirmation confirmation =
                new MockFundingConfirmation(
                        fundingReference,
                        providerReference,
                        true
                );

        FundingResponse expectedResponse = mock(FundingResponse.class);

        when(fundingConfirmationService.confirmFunding(
                fundingReference,
                FundingProvider.MOCK,
                providerReference,
                true
        )).thenReturn(expectedResponse);

        FundingResponse response =
                mockFundingConfirmationService.handle(confirmation);

        assertThat(response).isSameAs(expectedResponse);

        verify(fundingConfirmationService).confirmFunding(
                fundingReference,
                FundingProvider.MOCK,
                providerReference,
                true
        );

        verifyNoMoreInteractions(fundingConfirmationService);
    }

    @Test
    void handle_shouldDelegateFailedConfirmation() {
        String fundingReference = "FND-" + UUID.randomUUID();
        String providerReference = "MOCK-" + UUID.randomUUID();

        MockFundingConfirmation confirmation =
                new MockFundingConfirmation(
                        fundingReference,
                        providerReference,
                        false
                );

        FundingResponse expectedResponse = mock(FundingResponse.class);

        when(fundingConfirmationService.confirmFunding(
                fundingReference,
                FundingProvider.MOCK,
                providerReference,
                false
        )).thenReturn(expectedResponse);

        FundingResponse response =
                mockFundingConfirmationService.handle(confirmation);

        assertThat(response).isSameAs(expectedResponse);

        verify(fundingConfirmationService).confirmFunding(
                fundingReference,
                FundingProvider.MOCK,
                providerReference,
                false
        );

        verifyNoMoreInteractions(fundingConfirmationService);
    }
};