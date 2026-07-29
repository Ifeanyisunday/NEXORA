package com.nexora.banking.transaction.specification;

import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> filter(

            UUID walletId,

            TransactionFilterRequest filter

    ) {

        return Specification
                .where(walletEquals(walletId))
                .and(typeEquals(filter.type()))
                .and(categoryEquals(filter.category()))
                .and(statusEquals(filter.status()))
                .and(createdAfter(filter.from()))
                .and(createdBefore(filter.to()))
                .and(minAmount(filter.minAmount()))
                .and(maxAmount(filter.maxAmount()));

    }

    private static Specification<Transaction> walletEquals(UUID walletId) {

        return (root, query, cb) ->
                cb.equal(root.get("wallet").get("id"), walletId);

    }

    private static Specification<Transaction> typeEquals(
            com.nexora.banking.transaction.enums.TransactionType type
    ) {

        if (type == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("type"), type);

    }

    private static Specification<Transaction> categoryEquals(
            com.nexora.banking.transaction.enums.TransactionCategory category
    ) {

        if (category == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("category"), category);

    }

    private static Specification<Transaction> statusEquals(
            com.nexora.banking.transaction.enums.TransactionStatus status
    ) {

        if (status == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("status"), status);

    }

    private static Specification<Transaction> createdAfter(
            java.time.Instant from
    ) {

        if (from == null)
            return null;

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );

    }

    private static Specification<Transaction> createdBefore(
            java.time.Instant to
    ) {

        if (to == null)
            return null;

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );

    }

    private static Specification<Transaction> minAmount(
            java.math.BigDecimal amount
    ) {

        if (amount == null)
            return null;

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("amount"),
                        amount
                );

    }

    private static Specification<Transaction> maxAmount(
            java.math.BigDecimal amount
    ) {

        if (amount == null)
            return null;

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("amount"),
                        amount
                );

    }

}