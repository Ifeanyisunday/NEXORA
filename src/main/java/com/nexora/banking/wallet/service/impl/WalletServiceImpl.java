package com.nexora.banking.wallet.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.banking.common.exception.ResourceNotFoundException;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.dto.response.WalletResponse;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.factory.WalletFactory;
import com.nexora.banking.wallet.repository.WalletRepository;
import com.nexora.banking.wallet.service.AccountNumberService;
import com.nexora.banking.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountNumberService accountNumberService;


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
                        new ResourceNotFoundException(
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
                        new ResourceNotFoundException(
                                "Wallet not found."
                        )
                );

        wallet.deposit(amount);

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
                        new ResourceNotFoundException(
                                "Wallet not found."
                        )
                );

        wallet.withdraw(amount);

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