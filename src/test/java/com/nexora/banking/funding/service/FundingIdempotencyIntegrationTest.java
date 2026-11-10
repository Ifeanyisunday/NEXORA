
package com.nexora.banking.funding.service;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.factory.FundingFactory;
import com.nexora.banking.funding.provider.FundingProviderClient;
import com.nexora.banking.funding.provider.FundingProviderResult;
import com.nexora.banking.funding.repository.FundingRepository;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class FundingIdempotencyIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private FundingService fundingService;

    @MockitoSpyBean
    private FundingProviderClient fundingProviderClient;

    @Test
    void shouldRejectDuplicateIdempotencyKeyForSameUser() {

        // Arrange: create a unique user
        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("idempotency-test-" + uniqueId + "@example.com");
        user.setPasswordHash("integration-test-hash");

        User persistedUser = userRepository.saveAndFlush(user);

        Wallet wallet = Wallet.builder()
                .user(persistedUser)
                .balance(new BigDecimal("10000.00"))
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

        String idempotencyKey = "IDEMPOTENCY-" + uniqueId;

        Funding firstFunding = FundingFactory.create(
                persistedUser,
                persistedWallet,
                new BigDecimal("5000.00"),
                FundingMethod.CARD,
                FundingProvider.MOCK,
                idempotencyKey
        );

        fundingRepository.saveAndFlush(firstFunding);

        Funding duplicateFunding = FundingFactory.create(
                persistedUser,
                persistedWallet,
                new BigDecimal("5000.00"),
                FundingMethod.CARD,
                FundingProvider.MOCK,
                idempotencyKey
        );

        // Assert: PostgreSQL rejects the duplicate
        assertThrows(
                DataIntegrityViolationException.class,
                () -> fundingRepository.saveAndFlush(duplicateFunding)
        );
    }

    @Test
    void shouldCreateOnlyOneFundingWhenConcurrentRequestsUseSameKey()
            throws Exception {

        // Arrange
        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("concurrent-funding-" + uniqueId + "@example.com");
        user.setPasswordHash("integration-test-hash");

        User persistedUser = userRepository.saveAndFlush(user);

        Wallet wallet = Wallet.builder()
                .user(persistedUser)
                .balance(new BigDecimal("10000.00"))
                .accountNumber(
                        "9" + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 9)
                )
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();

        walletRepository.saveAndFlush(wallet);

        String idempotencyKey = "CONCURRENT-" + uniqueId;

        CreateFundingRequest request = new CreateFundingRequest(
                new BigDecimal("5000.00"),
                FundingMethod.CARD
        );

        CountDownLatch providerStarted = new CountDownLatch(1);
        CountDownLatch allowProviderToFinish = new CountDownLatch(1);

        AtomicInteger providerCallCount = new AtomicInteger();

        /*
         * Pause the first provider call.
         *
         * This gives the second request time to arrive
         * while the first request is still processing.
         */
        doAnswer(invocation -> {

            providerCallCount.incrementAndGet();

            providerStarted.countDown();

            boolean released = allowProviderToFinish.await(
                    10,
                    TimeUnit.SECONDS
            );

            if (!released) {
                throw new IllegalStateException(
                        "Provider test timed out waiting for release."
                );
            }

            return new FundingProviderResult(
                    true,
                    "MOCK-CONCURRENT-" + uniqueId
            );

        }).when(fundingProviderClient).initiate(any(Funding.class));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {

            // Act: start the first request
            Future<com.nexora.banking.funding.dto.response.FundingResponse>
                    firstRequest = executor.submit(() ->
                    fundingService.initiateFunding(
                            persistedUser,
                            request,
                            idempotencyKey
                    )
            );

            // Wait until the first request reaches the provider
            assertTrue(
                    providerStarted.await(10, TimeUnit.SECONDS),
                    "The first request did not reach the provider."
            );

            /*
             * Start the second request while the first
             * request is still waiting for the provider.
             */
            Future<com.nexora.banking.funding.dto.response.FundingResponse>
                    secondRequest = executor.submit(() ->
                    fundingService.initiateFunding(
                            persistedUser,
                            request,
                            idempotencyKey
                    )
            );

            /*
             * The second request should return the existing
             * reservation without calling the provider again.
             */
            var secondResponse = secondRequest.get(
                    10,
                    TimeUnit.SECONDS
            );

            // Allow the first provider request to finish
            allowProviderToFinish.countDown();

            var firstResponse = firstRequest.get(
                    10,
                    TimeUnit.SECONDS
            );

            // Assert: both requests refer to the same funding
            assertEquals(
                    firstResponse.reference(),
                    secondResponse.reference()
            );

            // Assert: only one provider call occurred
            assertEquals(1, providerCallCount.get());

            verify(fundingProviderClient, times(1))
                    .initiate(any(Funding.class));

            // Assert: exactly one reservation exists for this key
            Funding persistedFunding = fundingRepository
                    .findByUserAndIdempotencyKey(
                            persistedUser,
                            idempotencyKey
                    )
                    .orElseThrow();

            assertEquals(
                    firstResponse.reference(),
                    persistedFunding.getReference()
            );

        } finally {

            // Never leave the provider thread blocked
            allowProviderToFinish.countDown();

            executor.shutdownNow();
        }
    }
}