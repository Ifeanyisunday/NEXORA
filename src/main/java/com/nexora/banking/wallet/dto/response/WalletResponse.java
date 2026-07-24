package com.nexora.banking.wallet.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;

public record WalletResponse(

        UUID id,

        UUID userId,

        BigDecimal balance,

        Currency currency,

        WalletStatus status

) {
}