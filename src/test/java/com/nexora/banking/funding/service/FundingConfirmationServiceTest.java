package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingConfirmationServiceTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private FundingConfirmationServiceImpl fundingConfirmationService;

    private Funding funding;
    private Wallet wallet;
    private String fundingReference;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());

        wallet = Wallet.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .accountNumber("1234567890")
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();

        fundingReference = "FND-" + UUID.randomUUID();

        funding = Funding.builder()
                .user(user)
                .wallet(wallet)
                .amount(new BigDecimal("500.00"))
                .currency(Currency.NGN)
                .reference(fundingReference)
                .status(FundingStatus.PROCESSING)
                .provider(FundingProvider.MOCK)
                .providerReference("MOCK-" + UUID.randomUUID())
                .method(FundingMethod.CARD)
                .build();
    }

    @Test
    void confirmFunding_shouldCreditWalletAndCreateLedgerTransaction() {
        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        when(walletRepository.findByIdForUpdate(wallet.getId()))
                .thenReturn(Optional.of(wallet));

        FundingResponse response =
                fundingConfirmationService.confirmFunding(
                        fundingReference,
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1500.00");

        assertThat(response.status())
                .isEqualTo(FundingStatus.COMPLETED);

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionService).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertThat(transaction.getType())
                .isEqualTo(TransactionType.CREDIT);

        assertThat(transaction.getCategory())
                .isEqualTo(TransactionCategory.DEPOSIT);

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("500.00");

        assertThat(transaction.getBalanceBefore())
                .isEqualByComparingTo("1000.00");

        assertThat(transaction.getBalanceAfter())
                .isEqualByComparingTo("1500.00");

        assertThat(transaction.getWallet())
                .isEqualTo(wallet);
    }

    @Test
    void confirmFunding_shouldNotCreditWalletTwice() {
        funding.markCompleted(java.time.Instant.now());

        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        FundingResponse response =
                fundingConfirmationService.confirmFunding(
                        fundingReference,
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );

        assertThat(response.status())
                .isEqualTo(FundingStatus.COMPLETED);

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionService);
    }

    @Test
    void confirmFunding_shouldRejectFundingThatIsNotProcessing() {
        funding.markFailed();

        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        assertThatThrownBy(
                () -> fundingConfirmationService.confirmFunding(
                        fundingReference,
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Only processing funding can be confirmed."
                );

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionService);
    }

    @Test
    void confirmFunding_shouldRejectMismatchedProviderReference() {
        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        assertThatThrownBy(
                () -> fundingConfirmationService.confirmFunding(
                        fundingReference,
                        FundingProvider.MOCK,
                        "MOCK-INVALID-REFERENCE",
                        true
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider reference does not match.");

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionService);
    }

    @Test
    void confirmFunding_shouldRejectMissingProvider() {
        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        assertThatThrownBy(
                () -> fundingConfirmationService.confirmFunding(
                        fundingReference,
                        null,
                        funding.getProviderReference(),
                        true
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Funding does not belong to this provider."
                );

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionService);
    }

    @Test
    void confirmFunding_shouldMarkFailedPaymentWithoutCreditingWallet() {
        when(fundingRepository.findByReferenceForUpdate(fundingReference))
                .thenReturn(Optional.of(funding));

        FundingResponse response =
                fundingConfirmationService.confirmFunding(
                        fundingReference,
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        false
                );

        assertThat(response.status())
                .isEqualTo(FundingStatus.FAILED);

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verify(fundingRepository)
                .findByReferenceForUpdate(fundingReference);

        verifyNoInteractions(walletRepository);
        verifyNoInteractions(transactionService);
    }
};