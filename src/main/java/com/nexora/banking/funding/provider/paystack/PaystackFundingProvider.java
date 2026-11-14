package com.nexora.banking.funding.provider.paystack;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.provider.FundingProviderClient;
import com.nexora.banking.funding.provider.FundingProviderResult;
import com.nexora.banking.wallet.enums.Currency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(
        prefix = "paystack",
        name = "enabled",
        havingValue = "true"
)
public class PaystackFundingProvider implements FundingProviderClient {

    private final RestClient paystackRestClient;

    public PaystackFundingProvider(
        @Qualifier("paystackRestClient") RestClient paystackRestClient
    ) {
        this.paystackRestClient = paystackRestClient;
    }

    @Override
    public FundingProvider provider() {
        return FundingProvider.PAYSTACK;
    }

    @Override
    public FundingProviderResult initiate(Funding funding) {

        String email = funding.getUser().getEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required for Paystack funding."
            );
        }

        long amount = convertToMinorUnits(
                funding.getAmount(),
                funding.getCurrency()
        );

        validateReference(funding.getReference());

        PaystackInitializeRequest request =
                new PaystackInitializeRequest(
                        email,
                        amount,
                        funding.getCurrency().name(),
                        funding.getReference()
                );

        PaystackInitializeResponse response =
                paystackRestClient.post()
                        .uri("/transaction/initialize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(PaystackInitializeResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Paystack returned an empty response."
            );
        }

        if (!response.status()) {
            return new FundingProviderResult(
                    false,
                    null,
                    null
            );
        }

        if (response.data() == null
                || response.data().authorizationUrl() == null
                || response.data().authorizationUrl().isBlank()
                || response.data().reference() == null
                || response.data().reference().isBlank()) {

            throw new IllegalStateException(
                    "Paystack returned an incomplete initialization response."
            );
        }

        if (!funding.getReference().equals(response.data().reference())) {
            throw new IllegalStateException(
                    "Paystack returned a reference that does not match Nexora."
            );
        }

        return new FundingProviderResult(
                true,
                response.data().reference(),
                response.data().authorizationUrl()
        );
    }

    private long convertToMinorUnits(
            BigDecimal amount,
            Currency currency
    ) {

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Funding amount must be greater than zero."
            );
        }

        BigDecimal minimumAmount = switch (currency) {
            case NGN -> new BigDecimal("50.00");
            case USD -> new BigDecimal("2.00");
            default -> throw new IllegalArgumentException(
                    "Paystack does not currently support funding in "
                            + currency
            );
        };

        if (amount.compareTo(minimumAmount) < 0) {
            throw new IllegalArgumentException(
                    "Minimum Paystack funding amount for "
                            + currency + " is " + minimumAmount
            );
        }

        try {
            long minorUnits = amount
                    .setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2)
                    .longValueExact();

            return minorUnits;

        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Funding amount cannot be represented in minor currency units.",
                    exception
            );
        }
    }

    private void validateReference(String reference) {

        if (reference == null
                || !reference.matches("[A-Za-z0-9.=\\-]+")) {

            throw new IllegalArgumentException(
                    "Funding reference contains characters not supported by Paystack."
            );
        }
    }
}
