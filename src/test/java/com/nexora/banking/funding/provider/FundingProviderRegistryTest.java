package com.nexora.banking.funding.provider;

import com.nexora.banking.funding.enums.FundingProvider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FundingProviderRegistryTest {

    @Test
    void shouldReturnClientRegisteredForRequestedProvider() {

        FundingProviderClient mockClient = mock(FundingProviderClient.class);

        when(mockClient.provider()).thenReturn(FundingProvider.MOCK);

        FundingProviderRegistry registry =
                new FundingProviderRegistry(List.of(mockClient));

        assertSame(
                mockClient,
                registry.getClient(FundingProvider.MOCK)
        );
    }

    @Test
    void shouldRejectProviderWithoutRegisteredClient() {

        FundingProviderClient mockClient = mock(FundingProviderClient.class);

        when(mockClient.provider()).thenReturn(FundingProvider.MOCK);

        FundingProviderRegistry registry =
                new FundingProviderRegistry(List.of(mockClient));

        assertThrows(
                IllegalStateException.class,
                () -> registry.getClient(FundingProvider.PAYSTACK)
        );
    }

    @Test
    void shouldRejectDuplicateProviderRegistrations() {

        FundingProviderClient firstClient = mock(FundingProviderClient.class);
        FundingProviderClient secondClient = mock(FundingProviderClient.class);

        when(firstClient.provider()).thenReturn(FundingProvider.MOCK);
        when(secondClient.provider()).thenReturn(FundingProvider.MOCK);

        assertThrows(
                IllegalStateException.class,
                () -> new FundingProviderRegistry(
                        List.of(firstClient, secondClient)
                )
        );
    }
}