package com.nexora.banking.funding.factory;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public final class FundingFactory {

    private FundingFactory() {
    }

    public static Funding create(
            User user,
            Wallet wallet,
            BigDecimal amount,
            FundingMethod method,
            FundingProvider provider
    ) {
        return Funding.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .currency(wallet.getCurrency())
                .method(method)
                .provider(provider)
                .status(FundingStatus.PENDING)
                .reference(generateFundingReference())
                .build();
    }

    private static String generateFundingReference() {
        return "FND-" + UUID.randomUUID();
    }
}