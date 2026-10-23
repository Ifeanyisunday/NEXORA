
package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.repository.FundingRepository;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.repository.TransactionRepository;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.user.repository.UserRepository;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
class FundingConfirmationIntegrationTest {

    @Autowired
    private FundingConfirmationService fundingConfirmationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoSpyBean
    private TransactionService transactionService;

    @Test
    void confirmFunding_shouldPersistWalletCreditAndLedgerTransaction() {
        Funding funding = createProcessingFunding();
        Wallet wallet = funding.getWallet();

        FundingResponse response =
                fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );

        assertThat(response.status())
                .isEqualTo(FundingStatus.COMPLETED);

        Wallet persistedWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertThat(persistedWallet.getBalance())
                .isEqualByComparingTo("1500.00");

        Funding persistedFunding = fundingRepository
                .findByReference(funding.getReference())
                .orElseThrow();

        assertThat(persistedFunding.getStatus())
                .isEqualTo(FundingStatus.COMPLETED);

        assertThat(persistedFunding.getCompletedAt())
                .isNotNull();

        Page<Transaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(transactions.getTotalElements())
                .isEqualTo(1);

        Transaction transaction = transactions.getContent().get(0);

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

        assertThat(transaction.getWallet().getId())
                .isEqualTo(wallet.getId());
    }

    @Test
    void confirmFunding_shouldNotPersistDuplicateCreditForRepeatedCallback() {
        Funding funding = createProcessingFunding();
        Wallet wallet = funding.getWallet();

        fundingConfirmationService.confirmFunding(
                funding.getReference(),
                FundingProvider.MOCK,
                funding.getProviderReference(),
                true
        );

        FundingResponse retryResponse =
                fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );

        assertThat(retryResponse.status())
                .isEqualTo(FundingStatus.COMPLETED);

        Wallet persistedWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertThat(persistedWallet.getBalance())
                .isEqualByComparingTo("1500.00");

        Page<Transaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(transactions.getTotalElements())
                .isEqualTo(1);
    }

    @Test
    void confirmFunding_shouldRollbackWalletAndFundingWhenLedgerSaveFails() {
        Funding funding = createProcessingFunding();
        Wallet wallet = funding.getWallet();

        doThrow(new IllegalStateException(
                "Simulated ledger persistence failure"
        )).when(transactionService).save(any(Transaction.class));

        assertThatThrownBy(() ->
                fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated ledger persistence failure");

        Wallet persistedWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertThat(persistedWallet.getBalance())
                .isEqualByComparingTo("1000.00");

        Funding persistedFunding = fundingRepository
                .findByReference(funding.getReference())
                .orElseThrow();

        assertThat(persistedFunding.getStatus())
                .isEqualTo(FundingStatus.PROCESSING);

        assertThat(persistedFunding.getCompletedAt())
                .isNull();

        Page<Transaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(transactions.getTotalElements())
                .isZero();
    }

    @Test
    void confirmFunding_shouldRollbackWhenPostgresRejectsDuplicateLedgerReference() {
        Funding funding = createProcessingFunding();
        Wallet wallet = funding.getWallet();

        // First, commit a valid ledger entry with a unique reference.
        String existingReference = "TXN-CONSTRAINT-" + UUID.randomUUID();

        Transaction existingTransaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT)
                .category(TransactionCategory.DEPOSIT)
                .amount(new BigDecimal("1.00"))
                .balanceBefore(new BigDecimal("999.00"))
                .balanceAfter(new BigDecimal("1000.00"))
                .reference(existingReference)
                .description("Existing transaction for constraint test")
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.saveAndFlush(existingTransaction);

        // Replace the normal ledger save with an actual database insert
        // using the already-existing reference. PostgreSQL must reject it.
        doAnswer(invocation -> {
            Transaction attemptedTransaction = invocation.getArgument(0);

            Transaction duplicateTransaction = Transaction.builder()
                    .wallet(attemptedTransaction.getWallet())
                    .type(attemptedTransaction.getType())
                    .category(attemptedTransaction.getCategory())
                    .amount(attemptedTransaction.getAmount())
                    .balanceBefore(attemptedTransaction.getBalanceBefore())
                    .balanceAfter(attemptedTransaction.getBalanceAfter())
                    .reference(existingReference)
                    .description("Deliberate duplicate reference")
                    .status(attemptedTransaction.getStatus())
                    .build();

            transactionRepository.saveAndFlush(duplicateTransaction);

            return null;
        }).when(transactionService).save(any(Transaction.class));

        assertThatThrownBy(() ->
                fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                )
        ).isInstanceOf(DataIntegrityViolationException.class);

        // Verify committed state by reloading entities from PostgreSQL.
        Wallet persistedWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertThat(persistedWallet.getBalance())
                .isEqualByComparingTo("1000.00");

        Funding persistedFunding = fundingRepository
                .findByReference(funding.getReference())
                .orElseThrow();

        assertThat(persistedFunding.getStatus())
                .isEqualTo(FundingStatus.PROCESSING);

        assertThat(persistedFunding.getCompletedAt())
                .isNull();

        Page<Transaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId(),
                                PageRequest.of(0, 10)
                        );

        // The original entry remains; the attempted entry was rolled back.
        assertThat(transactions.getTotalElements())
                .isEqualTo(1);

        assertThat(transactions.getContent().get(0).getReference())
                .isEqualTo(existingReference);
    }

    private Funding createProcessingFunding() {
        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("funding-test-" + uniqueId + "@example.com");
        user.setPasswordHash("integration-test-hash");

        User persistedUser = userRepository.saveAndFlush(user);

        Wallet wallet = Wallet.builder()
                .user(persistedUser)
                .balance(new BigDecimal("1000.00"))
                .accountNumber(
                        "9" + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 9)
                )
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet persistedWallet = walletRepository.saveAndFlush(wallet);

        Funding funding = Funding.builder()
                .user(persistedUser)
                .wallet(persistedWallet)
                .amount(new BigDecimal("500.00"))
                .currency(Currency.NGN)
                .reference("FND-" + uniqueId)
                .status(FundingStatus.PROCESSING)
                .provider(FundingProvider.MOCK)
                .providerReference("MOCK-" + uniqueId)
                .method(FundingMethod.CARD)
                .build();

        return fundingRepository.saveAndFlush(funding);
    }


    @Test
    void concurrentSuccessfulCallbacks_shouldCreditWalletOnlyOnce()
            throws Exception {

        // Arrange: create one processing funding record and its wallet.
        Funding funding = createProcessingFunding();
        Wallet wallet = funding.getWallet();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Both workers wait until they are ready before starting.
            Future<FundingResponse> firstCallback = executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Timed out waiting to start first callback."
                    );
                }

                return fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );
            });

            Future<FundingResponse> secondCallback = executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Timed out waiting to start second callback."
                    );
                }

                return fundingConfirmationService.confirmFunding(
                        funding.getReference(),
                        FundingProvider.MOCK,
                        funding.getProviderReference(),
                        true
                );
            });

            // Ensure both worker threads are ready before releasing them.
            assertThat(ready.await(10, TimeUnit.SECONDS))
                    .as("Both callback workers should be ready")
                    .isTrue();

            // Release both callbacks as close together as possible.
            start.countDown();

            // get() propagates worker failures and imposes a timeout.
            firstCallback.get(20, TimeUnit.SECONDS);
            secondCallback.get(20, TimeUnit.SECONDS);

        } finally {
            // Always release blocked workers and shut down the executor.
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS))
                    .as("Callback workers should terminate")
                    .isTrue();
        }

        // Assert: reload committed state from the database.
        Wallet persistedWallet = walletRepository
                .findById(wallet.getId())
                .orElseThrow();

        assertThat(persistedWallet.getBalance())
                .isEqualByComparingTo("1500.00");

        Funding persistedFunding = fundingRepository
                .findByReference(funding.getReference())
                .orElseThrow();

        assertThat(persistedFunding.getStatus())
                .isEqualTo(FundingStatus.COMPLETED);

        assertThat(persistedFunding.getCompletedAt())
                .isNotNull();

        Page<Transaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(transactions.getTotalElements())
                .isEqualTo(1);

        assertThat(transactions.getContent().get(0).getAmount())
                .isEqualByComparingTo("500.00");
    }
}