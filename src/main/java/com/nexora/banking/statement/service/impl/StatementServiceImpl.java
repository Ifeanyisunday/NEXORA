package com.nexora.banking.statement.service.impl;

import com.nexora.banking.common.exception.WalletNotFoundException;
import com.nexora.banking.statement.dto.response.StatementItemResponse;
import com.nexora.banking.statement.dto.response.StatementResponse;
import com.nexora.banking.statement.service.StatementService;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.repository.TransactionRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final WalletRepository walletRepository;

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public StatementResponse generateStatement(
            User user,
            LocalDate from,
            LocalDate to
    ) {

        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(
                        () -> new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        Instant startDate = from
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        Instant endDate = to
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        List<Transaction> transactions =
                transactionRepository
                        .findByWalletIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                                wallet.getId(),
                                startDate,
                                endDate
                        );

        Transaction firstTransaction =
                transactions.stream()
                        .findFirst()
                        .orElse(null);

        BigDecimal openingBalance;

        if (firstTransaction == null) {

            openingBalance = wallet.getBalance();

        } else {

            openingBalance =
                    firstTransaction.getBalanceBefore();
        }

        BigDecimal closingBalance;

        if (transactions.isEmpty()) {

            closingBalance = wallet.getBalance();

        } else {

            closingBalance =
                    transactions.get(
                            transactions.size() - 1
                    ).getBalanceAfter();
        }

        BigDecimal totalCredits = BigDecimal.ZERO;

        BigDecimal totalDebits = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            if (
                    transaction.getType()
                            == TransactionType.CREDIT
            ) {

                totalCredits =
                        totalCredits.add(
                                transaction.getAmount()
                        );
            }

            if (
                    transaction.getType()
                            == TransactionType.DEBIT
            ) {

                totalDebits =
                        totalDebits.add(
                                transaction.getAmount()
                        );
            }
        }

        List<StatementItemResponse> items =
                transactions.stream()
                        .map(transaction -> {

                            BigDecimal debit = BigDecimal.ZERO;
                            BigDecimal credit = BigDecimal.ZERO;

                            if (
                                    transaction.getType()
                                            == TransactionType.DEBIT
                            ) {

                                debit = transaction.getAmount();

                            } else {

                                credit = transaction.getAmount();
                            }

                            return new StatementItemResponse(
                                    transaction.getCreatedAt(),
                                    transaction.getReference(),
                                    transaction.getDescription(),
                                    debit,
                                    credit,
                                    transaction.getBalanceAfter()
                            );
                        })
                        .toList();

        return new StatementResponse(

                generateStatementReference(),

                Instant.now(),

                startDate,

                endDate,

                openingBalance,

                closingBalance,

                totalCredits,

                totalDebits,

                items
        );
    }

    private String generateStatementReference() {
        return "STM-" + UUID.randomUUID();
    }
}