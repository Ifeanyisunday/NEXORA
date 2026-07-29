package com.nexora.banking.transaction.factory;

import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionStatus;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.wallet.entity.Wallet;

import java.math.BigDecimal;

public final class TransactionFactory {

    private TransactionFactory() {
    }

    public static Transaction create(

            Wallet wallet,

            Transfer transfer,

            TransactionType type,

            BigDecimal balanceBefore,

            BigDecimal balanceAfter,

            String description

    ) {

        return Transaction.builder()
                .wallet(wallet)
                .transfer(transfer)
                .type(type)
                .category(TransactionCategory.TRANSFER)
                .amount(transfer.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .reference(transfer.getReference())
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();

    }
    

}