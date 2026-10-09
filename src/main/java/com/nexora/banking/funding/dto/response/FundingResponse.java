package com.nexora.banking.funding.dto.response;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.wallet.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundingResponse(
        UUID id,
        String reference,
        BigDecimal amount,
        Currency currency,
        FundingMethod method,
        FundingProvider provider,
        FundingStatus status,
        String providerReference,
        Instant createdAt,
        Instant completedAt
) {

    public static FundingResponse from(Funding funding) {
        return new FundingResponse(
                funding.getId(),
                funding.getReference(),
                funding.getAmount(),
                funding.getCurrency(),
                funding.getMethod(),
                funding.getProvider(),
                funding.getStatus(),
                funding.getProviderReference(),
                funding.getCreatedAt(),
                funding.getCompletedAt()
        );
    }
}