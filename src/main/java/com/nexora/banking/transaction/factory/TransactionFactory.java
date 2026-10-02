package com.nexora.banking.transaction.factory;

import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransactionFactory {

    private TransactionFactory() {
    }

    public static Transaction create(
            Wallet wallet,
            TransactionType type,
            TransactionCategory category,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description
    ) {
        return create(
                wallet,
                null,
                type,
                category,
                amount,
                balanceBefore,
                balanceAfter,
                description
        );
    }

    public static Transaction create(
            Wallet wallet,
            Transfer transfer,
            TransactionType type,
            TransactionCategory category,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description
    ) {
        return Transaction.builder()
                .wallet(wallet)
                .transfer(transfer)
                .type(type)
                .category(category)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .reference(generateTransactionReference())
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    private static String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID();
    }
}