package com.nexora.banking.wallet.factory;

import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Currency;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.entity.WalletStatus;

import java.math.BigDecimal;

@SuppressWarnings("null")
public final class WalletFactory {
    private WalletFactory() {
    }

    public static Wallet create(User user) {

        return Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();
    }

}