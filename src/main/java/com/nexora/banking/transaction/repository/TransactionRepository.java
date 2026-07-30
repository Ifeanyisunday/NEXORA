package com.nexora.banking.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexora.banking.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID>, 
                JpaSpecificationExecutor<Transaction>{

            Page<Transaction> findByWalletUserId(
                UUID userId,
                Pageable pageable
            );

            Page<Transaction> findByWalletIdOrderByCreatedAtDesc(
                UUID walletId,
                Pageable pageable
            );

            List<Transaction> 
            findByWalletIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                UUID walletId,
                Instant from,
                Instant to
            );

            Optional<Transaction> findByReference(
                String reference
            );

        }