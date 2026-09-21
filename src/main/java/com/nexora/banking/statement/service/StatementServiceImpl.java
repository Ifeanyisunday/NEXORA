package com.nexora.banking.statement.service;

import com.nexora.banking.common.exception.WalletNotFoundException;
import com.nexora.banking.statement.dto.response.StatementItemResponse;
import com.nexora.banking.statement.dto.response.StatementResponse;
import com.nexora.banking.statement.validator.StatementDateValidator;

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
    private final StatementDateValidator statementDateValidator;

    @Override
    @Transactional(readOnly = true)
    public StatementResponse generateStatement(
            User user,
            LocalDate from,
            LocalDate to
    ) {

        statementDateValidator.validate(from, to);
        
        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(
                        () -> new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        /*
         * Convert the requested calendar dates into UTC
         * boundaries.
         *
         * Example:
         *
         * from = 2026-09-01
         * to   = 2026-09-05
         *
         * startDate = 2026-09-01T00:00:00Z
         * endDate   = 2026-09-06T00:00:00Z
         *
         * The statement interval is:
         *
         * [startDate, endDate)
         */
        Instant startDate = from
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        Instant endDate = to
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        /*
         * Find all transactions belonging to the
         * requested statement period.
         */
        List<Transaction> transactions =
                transactionRepository.findForStatement(
                        wallet.getId(),
                        startDate,
                        endDate
                );

        /*
         * Find the most recent transaction BEFORE
         * the statement period.
         *
         * Its balanceAfter represents the opening
         * balance of the statement.
         */
        Transaction previousTransaction =
                transactionRepository
                        .findFirstByWalletIdAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
                                wallet.getId(),
                                startDate
                        )
                        .orElse(null);

        BigDecimal openingBalance;

        if (previousTransaction != null) {

            openingBalance =
                    previousTransaction.getBalanceAfter();

        } else if (!transactions.isEmpty()) {

            /*
             * There is no transaction before the statement,
             * so the first transaction's balanceBefore is
             * the opening balance.
             */
            openingBalance =
                    transactions.get(0).getBalanceBefore();

        } else {

            /*
             * No transaction exists before or during the
             * statement period.
             *
             * The wallet has no ledger history from which
             * to reconstruct an earlier balance.
             *
             * In this situation the wallet's current balance
             * is the only available balance.
             */
            openingBalance =
                    wallet.getBalance();
        }

        /*
         * Closing balance.
         *
         * If transactions occurred during the period,
         * the last transaction's balanceAfter is the
         * historical closing balance.
         *
         * If no transactions occurred, the balance did
         * not change during the period, so closing equals
         * opening.
         */
        BigDecimal closingBalance;

        if (!transactions.isEmpty()) {

            closingBalance =
                    transactions
                            .get(transactions.size() - 1)
                            .getBalanceAfter();

        } else {

            closingBalance = openingBalance;
        }

        /*
         * Calculate total credits and debits.
         */
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            if (transaction.getType() == TransactionType.CREDIT) {

                totalCredits =
                        totalCredits.add(
                                transaction.getAmount()
                        );

            } else if (
                    transaction.getType()
                            == TransactionType.DEBIT
            ) {

                totalDebits =
                        totalDebits.add(
                                transaction.getAmount()
                        );
            }
        }

        /*
         * Convert ledger transactions into statement rows.
         */
        List<StatementItemResponse> items =
                transactions.stream()
                        .map(transaction -> {

                            BigDecimal debit = BigDecimal.ZERO;
                            BigDecimal credit = BigDecimal.ZERO;

                            if (
                                    transaction.getType()
                                            == TransactionType.DEBIT
                            ) {

                                debit =
                                        transaction.getAmount();

                            } else if (
                                    transaction.getType()
                                            == TransactionType.CREDIT
                            ) {

                                credit =
                                        transaction.getAmount();
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