package com.nexora.banking.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.banking.wallet.exceptions.WalletNotFoundException;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.factory.TransactionFactory;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.dto.response.WalletResponse;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.factory.WalletFactory;
import com.nexora.banking.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountNumberService accountNumberService;
    private final TransactionService transactionService;

    @Override
    public Wallet createWallet(User user) {

        String accountNumber =
                accountNumberService.generateAccountNumber();

        Wallet wallet = WalletFactory.create(
                user,
                accountNumber
        );

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(UUID userId) {

        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        return toResponse(wallet);
    }

    @Override
    public WalletResponse deposit(
            UUID userId,
            BigDecimal amount
    ) {

        Wallet wallet = walletRepository
                .findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        BigDecimal balanceBefore =
                wallet.getBalance();

        wallet.deposit(amount);

        BigDecimal balanceAfter =
                wallet.getBalance();

        Transaction transaction =
                TransactionFactory.create(
                        wallet,
                        TransactionType.CREDIT,
                        TransactionCategory.DEPOSIT,
                        amount,
                        balanceBefore,
                        balanceAfter,
                        "Wallet deposit"
                );

        transactionService.save(transaction);

        return toResponse(wallet);
    }

    @Override
    public WalletResponse withdraw(
            UUID userId,
            BigDecimal amount
    ) {

        Wallet wallet = walletRepository
                .findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        BigDecimal balanceBefore =
                wallet.getBalance();

        wallet.withdraw(amount);

        BigDecimal balanceAfter =
                wallet.getBalance();

        Transaction transaction =
                TransactionFactory.create(
                        wallet,
                        TransactionType.DEBIT,
                        TransactionCategory.WITHDRAWAL,
                        amount,
                        balanceBefore,
                        balanceAfter,
                        "Wallet withdrawal"
                );

        transactionService.save(transaction);

        return toResponse(wallet);
    }

    private WalletResponse toResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getAccountNumber(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus()
        );
    }
}