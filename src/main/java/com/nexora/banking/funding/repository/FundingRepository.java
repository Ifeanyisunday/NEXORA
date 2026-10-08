package com.nexora.banking.funding.repository;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundingRepository
        extends JpaRepository<Funding, UUID> {

    Optional<Funding> findByReference(String reference);

    Optional<Funding> findByProviderAndProviderReference(
            FundingProvider provider,
            String providerReference
    );

    boolean existsByReference(String reference);

    boolean existsByProviderAndProviderReference(
            FundingProvider provider,
            String providerReference
    );
}