package com.nexora.banking.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.nexora.banking.wallet.dto.response.WalletResponse;

public interface WalletService {

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