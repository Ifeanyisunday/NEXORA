package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.exception.FundingIdempotencyKeyConflictException;
import com.nexora.banking.funding.provider.FundingProviderClient;
import com.nexora.banking.funding.provider.FundingProviderRegistry;
import com.nexora.banking.funding.provider.FundingProviderResult;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.dao.DataIntegrityViolationException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingServiceImplTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingReservationService fundingReservationService;

    @Mock
    private FundingProviderRegistry fundingProviderRegistry;

    @Mock
    private FundingProviderClient fundingProviderClient;

    @InjectMocks
    private FundingServiceImpl fundingService;

    private User currentUser;
    private CreateFundingRequest request;
    private Funding funding;

    @BeforeEach
    void setUp() {

        currentUser = mock(User.class);

        request = new CreateFundingRequest(
                new BigDecimal("5000.00"),
                FundingMethod.CARD,
                FundingProvider.MOCK
        );

        funding = Funding.builder()
                .user(currentUser)
                .amount(new BigDecimal("5000.00"))
                .method(FundingMethod.CARD)
                .provider(FundingProvider.MOCK)
                .reference("FND-TEST-001")
                .idempotencyKey("TEST-KEY-001")
                .status(FundingStatus.PENDING)
                .build();
    }

    @Test
    void shouldInitiateFundingWhenReservationIsNew() {

        String idempotencyKey = "TEST-KEY-001";

        when(fundingReservationService.reserve(
                currentUser,
                request,
                idempotencyKey
        )).thenReturn(
                new FundingReservationService.Reservation(funding, true)
        );

        when(fundingProviderRegistry.getClient(FundingProvider.MOCK))
                .thenReturn(fundingProviderClient);

        when(fundingProviderClient.initiate(funding))
                .thenReturn(new FundingProviderResult(true, "MOCK-REF-001"));

        when(fundingRepository.save(funding)).thenReturn(funding);

        var response = fundingService.initiateFunding(
                currentUser,
                request,
                idempotencyKey
        );

        assertNotNull(response);

        assertEquals(
                FundingStatus.PROCESSING,
                funding.getStatus()
        );

        assertEquals(
                "MOCK-REF-001",
                funding.getProviderReference()
        );

        verify(fundingProviderRegistry)
                .getClient(FundingProvider.MOCK);

        verify(fundingProviderClient).initiate(funding);
        verify(fundingRepository).save(funding);
    }

    @Test
    void shouldReturnExistingFundingWithoutCallingProvider() {

        String idempotencyKey = "TEST-KEY-001";

        when(fundingReservationService.reserve(
                currentUser,
                request,
                idempotencyKey
        )).thenReturn(
                new FundingReservationService.Reservation(funding, false)
        );

        var response = fundingService.initiateFunding(
                currentUser,
                request,
                idempotencyKey
        );

        assertNotNull(response);

        verifyNoInteractions(fundingProviderRegistry);
        verify(fundingProviderClient, never()).initiate(any());
        verify(fundingRepository, never()).save(any());
    }

    @Test
    void shouldMarkFundingAsFailedWhenProviderRejectsRequest() {

        String idempotencyKey = "TEST-KEY-001";

        when(fundingReservationService.reserve(
                currentUser,
                request,
                idempotencyKey
        )).thenReturn(
                new FundingReservationService.Reservation(funding, true)
        );

        when(fundingProviderRegistry.getClient(FundingProvider.MOCK))
                .thenReturn(fundingProviderClient);

        when(fundingProviderClient.initiate(funding))
                .thenReturn(new FundingProviderResult(false, null));

        when(fundingRepository.save(funding)).thenReturn(funding);

        var response = fundingService.initiateFunding(
                currentUser,
                request,
                idempotencyKey
        );

        assertNotNull(response);

        assertEquals(
                FundingStatus.FAILED,
                funding.getStatus()
        );

        verify(fundingProviderRegistry)
                .getClient(FundingProvider.MOCK);

        verify(fundingProviderClient).initiate(funding);
        verify(fundingRepository).save(funding);
    }

    @Test
    void shouldReturnExistingFundingWhenConcurrentRequestWinsReservation() {

        String idempotencyKey = "TEST-KEY-001";

        when(fundingReservationService.reserve(
                currentUser,
                request,
                idempotencyKey
        )).thenThrow(
                new DataIntegrityViolationException("Duplicate idempotency key")
        );

        when(fundingRepository.findByUserAndIdempotencyKey(
                currentUser,
                idempotencyKey
        )).thenReturn(Optional.of(funding));

        var response = fundingService.initiateFunding(
                currentUser,
                request,
                idempotencyKey
        );

        assertNotNull(response);
        assertEquals(funding.getReference(), response.reference());

        verify(fundingReservationService)
                .validateSameRequest(funding, request);

        verifyNoInteractions(fundingProviderRegistry);
        verify(fundingProviderClient, never()).initiate(any());
    }

    @Test
    void shouldRejectConcurrentRequestWhenIdempotencyKeyHasDifferentRequest() {

        String idempotencyKey = "TEST-KEY-001";

        when(fundingReservationService.reserve(
                currentUser,
                request,
                idempotencyKey
        )).thenThrow(
                new DataIntegrityViolationException("Duplicate idempotency key")
        );

        when(fundingRepository.findByUserAndIdempotencyKey(
                currentUser,
                idempotencyKey
        )).thenReturn(Optional.of(funding));

        doThrow(new FundingIdempotencyKeyConflictException(
                "This idempotency key has already been used with a different funding request."
        )).when(fundingReservationService)
                .validateSameRequest(funding, request);

        assertThrows(
                FundingIdempotencyKeyConflictException.class,
                () -> fundingService.initiateFunding(
                        currentUser,
                        request,
                        idempotencyKey
                )
        );

        verifyNoInteractions(fundingProviderRegistry);
        verify(fundingProviderClient, never()).initiate(any());
    }
}