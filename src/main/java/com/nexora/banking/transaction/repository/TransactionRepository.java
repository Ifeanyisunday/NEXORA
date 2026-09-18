package com.nexora.banking.transaction.repository;

import com.nexora.banking.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findByWalletUserId(
            UUID userId,
            Pageable pageable
    );

    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(
            UUID walletId,
            Pageable pageable
    );

    /*
     * Transactions inside a statement period.
     *
     * Uses a half-open interval:
     *
     * createdAt >= from
     * createdAt < to
     *
     * This prevents a transaction exactly at midnight
     * on the following day from being included.
     */
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.wallet.id = :walletId
              AND t.createdAt >= :from
              AND t.createdAt < :to
            ORDER BY t.createdAt ASC, t.id ASC
            """)
    List<Transaction> findForStatement(
            @Param("walletId") UUID walletId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /*
     * Finds the most recent transaction before the
     * statement period begins.
     *
     * Its balanceAfter represents the wallet balance
     * immediately before the statement period.
     */
    Optional<Transaction> findFirstByWalletIdAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
            UUID walletId,
            Instant from
    );

    List<Transaction> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            UUID walletId,
            Instant from,
            Instant to
    );

    Optional<Transaction> findByReference(String reference);
}