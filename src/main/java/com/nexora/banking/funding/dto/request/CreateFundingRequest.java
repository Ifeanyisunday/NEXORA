package com.nexora.banking.funding.dto.request;

import com.nexora.banking.funding.enums.FundingMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateFundingRequest(

        @NotNull(message = "Amount is required.")
        @DecimalMin(
                value = "0.00",
                inclusive = false,
                message = "Amount must be greater than zero."
        )
        BigDecimal amount,

        @NotNull(message = "Funding method is required.")
        FundingMethod method

) {
}