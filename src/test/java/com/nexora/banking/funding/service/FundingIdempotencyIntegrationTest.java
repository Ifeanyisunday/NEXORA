package com.nexora.banking.funding.service;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.factory.FundingFactory;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class FundingIdempotencyIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Test
    void shouldRejectDuplicateIdempotencyKeyForSameUser() {

        // Arrange: create a unique user
        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("idempotency-test-" + uniqueId + "@example.com");
        user.setPasswordHash("integration-test-hash");

        User persistedUser = userRepository.saveAndFlush(user);

        // Create the user's wallet
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

        // Create and persist the first funding
        Funding firstFunding = FundingFactory.create(
                persistedUser,
                persistedWallet,
                new BigDecimal("5000.00"),
                FundingMethod.CARD,
                FundingProvider.MOCK,
                idempotencyKey
        );

        fundingRepository.saveAndFlush(firstFunding);

        // Create another funding with the same user and key
        Funding duplicateFunding = FundingFactory.create(
                persistedUser,
                persistedWallet,
                new BigDecimal("5000.00"),
                FundingMethod.CARD,
                FundingProvider.MOCK,
                idempotencyKey
        );

        // Act and Assert: PostgreSQL must reject the duplicate
        assertThrows(
                DataIntegrityViolationException.class,
                () -> fundingRepository.saveAndFlush(duplicateFunding)
        );
    }
}
