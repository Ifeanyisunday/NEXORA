package com.nexora.banking.wallet.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WalletAmountRequest(

        @NotNull(message = "Amount is required.")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero."
        )
        BigDecimal amount

) {
}