
package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.exception.FundingIdempotencyKeyConflictException;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingReservationServiceTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private FundingReservationService fundingReservationService;

    private User currentUser;
    private Wallet wallet;
    private Funding existingFunding;
    private CreateFundingRequest request;

    private static final String IDEMPOTENCY_KEY = "TEST-KEY-001";

    @BeforeEach
    void setUp() {

        currentUser = mock(User.class);

        wallet = Wallet.builder()
                .user(currentUser)
                .balance(new BigDecimal("10000.00"))
                .accountNumber("1234567890")
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();

        request = new CreateFundingRequest(
                new BigDecimal("5000.00"),
                FundingMethod.CARD
        );

        existingFunding = Funding.builder()
                .user(currentUser)
                .wallet(wallet)
                .amount(new BigDecimal("5000.00"))
                .currency(Currency.NGN)
                .method(FundingMethod.CARD)
                .provider(FundingProvider.MOCK)
                .reference("FND-TEST-001")
                .idempotencyKey(IDEMPOTENCY_KEY)
                .status(FundingStatus.PENDING)
                .build();
    }

    @Test
    void shouldReturnExistingReservationWhenIdempotencyKeyAlreadyExists() {

        // Arrange
        when(fundingRepository.findByUserAndIdempotencyKey(
                currentUser,
                IDEMPOTENCY_KEY
        )).thenReturn(Optional.of(existingFunding));

        // Act
        var reservation = fundingReservationService.reserve(
                currentUser,
                request,
                IDEMPOTENCY_KEY
        );

        // Assert
        assertSame(existingFunding, reservation.funding());
        assertFalse(reservation.newlyCreated());

        verify(fundingRepository, never()).saveAndFlush(any());
        verify(walletRepository, never()).findByUser(any());
    }

    @Test
    void shouldRejectSameKeyWhenAmountIsDifferent() {

        // Arrange
        CreateFundingRequest differentAmountRequest =
                new CreateFundingRequest(
                        new BigDecimal("7000.00"),
                        FundingMethod.CARD
                );

        // Act and Assert
        assertThrows(
                FundingIdempotencyKeyConflictException.class,
                () -> fundingReservationService.validateSameRequest(
                        existingFunding,
                        differentAmountRequest
                )
        );
    }

    @Test
    void shouldRejectSameKeyWhenFundingMethodIsDifferent() {

        // Arrange
        CreateFundingRequest differentMethodRequest =
                new CreateFundingRequest(
                        new BigDecimal("5000.00"),
                        FundingMethod.BANK_TRANSFER
                );

        // Act and Assert
        assertThrows(
                FundingIdempotencyKeyConflictException.class,
                () -> fundingReservationService.validateSameRequest(
                        existingFunding,
                        differentMethodRequest
                )
        );
    }

    @Test
    void shouldCreateNewReservationWhenIdempotencyKeyDoesNotExist() {

        // Arrange
        when(fundingRepository.findByUserAndIdempotencyKey(
                currentUser,
                IDEMPOTENCY_KEY
        )).thenReturn(Optional.empty());

        when(walletRepository.findByUser(currentUser))
                .thenReturn(Optional.of(wallet));

        when(fundingRepository.saveAndFlush(any(Funding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var reservation = fundingReservationService.reserve(
                currentUser,
                request,
                IDEMPOTENCY_KEY
        );

        // Assert
        assertNotNull(reservation.funding());
        assertTrue(reservation.newlyCreated());

        assertEquals(
                FundingStatus.PENDING,
                reservation.funding().getStatus()
        );

        assertEquals(
                IDEMPOTENCY_KEY,
                reservation.funding().getIdempotencyKey()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                reservation.funding().getAmount()
        );

        assertEquals(
                Currency.NGN,
                reservation.funding().getCurrency()
        );

        assertEquals(
                FundingProvider.MOCK,
                reservation.funding().getProvider()
        );

        verify(fundingRepository, times(1))
                .saveAndFlush(any(Funding.class));
    }

    @Test
    void shouldRejectReservationWhenWalletIsInactive() {

        // Arrange
        Wallet inactiveWallet = Wallet.builder()
                .user(currentUser)
                .balance(new BigDecimal("10000.00"))
                .accountNumber("1234567890")
                .currency(Currency.NGN)
                .status(WalletStatus.FROZEN)
                .build();

        when(fundingRepository.findByUserAndIdempotencyKey(
                currentUser,
                IDEMPOTENCY_KEY
        )).thenReturn(Optional.empty());

        when(walletRepository.findByUser(currentUser))
                .thenReturn(Optional.of(inactiveWallet));

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> fundingReservationService.reserve(
                        currentUser,
                        request,
                        IDEMPOTENCY_KEY
                )
        );

        verify(fundingRepository, never()).saveAndFlush(any());
    }
}
