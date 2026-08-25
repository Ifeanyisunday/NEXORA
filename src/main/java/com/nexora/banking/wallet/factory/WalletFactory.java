package com.nexora.banking.wallet.factory;

import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;

import java.math.BigDecimal;

@SuppressWarnings("null")
public final class WalletFactory {
    private WalletFactory() {
    }

    public static Wallet create(
        User user,
        String accountNumber
    ) {

        return Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .accountNumber(accountNumber)
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();
    }

}