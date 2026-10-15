package com.nexora.banking.funding.repository;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FundingRepository extends JpaRepository<Funding, UUID> {

    Optional<Funding> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Funding f
            WHERE f.reference = :reference
            """)
    Optional<Funding> findByReferenceForUpdate(
            @Param("reference") String reference
    );

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