package com.nexora.banking.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.nexora.banking.wallet.dto.response.WalletResponse;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.user.entity.User;

public interface WalletService {

    Wallet createWallet(User user);

    WalletResponse getMyWallet(UUID userId);

    WalletResponse deposit(
            UUID userId,
            BigDecimal amount
    );

    WalletResponse withdraw(
            UUID userId,
            BigDecimal amount
    );
}