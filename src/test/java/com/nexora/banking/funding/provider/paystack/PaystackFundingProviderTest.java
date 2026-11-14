package com.nexora.banking.funding.provider.paystack;

import com.nexora.banking.funding.entity.Funding;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.enums.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class PaystackFundingProviderTest {

    private static final String BASE_URL = "https://api.paystack.co";

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private PaystackFundingProvider provider;

    private User user;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL);

        mockServer = MockRestServiceServer.bindTo(builder).build();

        restClient = builder.build();

        provider = new PaystackFundingProvider(restClient);

        user = mock(User.class);

        when(user.getEmail()).thenReturn("customer@example.com");
    }

    @Test
    void shouldInitializeNgnFundingSuccessfully() {

        Funding funding = createFunding(
                new BigDecimal("50000.00"),
                Currency.NGN
        );

        mockServer.expect(requestTo(BASE_URL + "/transaction/initialize"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.email")
                        .value("customer@example.com"))
                .andExpect(jsonPath("$.amount")
                        .value(5000000))
                .andExpect(jsonPath("$.currency")
                        .value("NGN"))
                .andExpect(jsonPath("$.reference")
                        .value("FND-TEST-001"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": true,
                          "message": "Authorization URL created",
                          "data": {
                            "authorization_url": "https://checkout.paystack.com/test123",
                            "access_code": "test_access_code",
                            "reference": "FND-TEST-001"
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        var result = provider.initiate(funding);

        assertTrue(result.successful());

        assertEquals(
                "FND-TEST-001",
                result.providerReference()
        );

        assertEquals(
                "https://checkout.paystack.com/test123",
                result.authorizationUrl()
        );

        assertEquals(
                FundingProvider.PAYSTACK,
                provider.provider()
        );

        mockServer.verify();
    }

    @Test
    void shouldConvertUsdToMinorUnitsCorrectly() {

        Funding funding = createFunding(
                new BigDecimal("25.50"),
                Currency.USD
        );

        mockServer.expect(requestTo(BASE_URL + "/transaction/initialize"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.amount").value(2550))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": true,
                          "message": "Authorization URL created",
                          "data": {
                            "authorization_url": "https://checkout.paystack.com/usd123",
                            "access_code": "usd_access_code",
                            "reference": "FND-TEST-001"
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        var result = provider.initiate(funding);

        assertTrue(result.successful());

        mockServer.verify();
    }

    @Test
    void shouldReturnUnsuccessfulResultWhenPaystackRejectsInitialization() {

        Funding funding = createFunding(
                new BigDecimal("50000.00"),
                Currency.NGN
        );

        mockServer.expect(requestTo(BASE_URL + "/transaction/initialize"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                        {
                          "status": false,
                          "message": "Unable to initialize transaction",
                          "data": null
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        var result = provider.initiate(funding);

        assertFalse(result.successful());
        assertNull(result.providerReference());
        assertNull(result.authorizationUrl());

        mockServer.verify();
    }

    @Test
    void shouldRejectUnsupportedCurrencyBeforeCallingPaystack() {

        Funding funding = createFunding(
                new BigDecimal("100.00"),
                Currency.EUR
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.initiate(funding)
        );

        mockServer.verify();
    }

    @Test
    void shouldRejectAmountBelowPaystackMinimum() {

        Funding funding = createFunding(
                new BigDecimal("49.99"),
                Currency.NGN
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.initiate(funding)
        );

        mockServer.verify();
    }

    @Test
    void shouldRejectAmountWithMoreThanTwoDecimalPlaces() {

        Funding funding = createFunding(
                new BigDecimal("500.001"),
                Currency.NGN
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.initiate(funding)
        );

        mockServer.verify();
    }

    @Test
    void shouldRejectFundingWithoutCustomerEmail() {

        when(user.getEmail()).thenReturn(" ");

        Funding funding = createFunding(
                new BigDecimal("50000.00"),
                Currency.NGN
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.initiate(funding)
        );

        mockServer.verify();
    }

    @Test
    void shouldRejectMismatchedProviderReference() {

        Funding funding = createFunding(
                new BigDecimal("50000.00"),
                Currency.NGN
        );

        mockServer.expect(requestTo(BASE_URL + "/transaction/initialize"))
                .andRespond(withSuccess(
                        """
                        {
                          "status": true,
                          "message": "Authorization URL created",
                          "data": {
                            "authorization_url": "https://checkout.paystack.com/test123",
                            "access_code": "test_access_code",
                            "reference": "SOME-OTHER-REFERENCE"
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                IllegalStateException.class,
                () -> provider.initiate(funding)
        );

        mockServer.verify();
    }

    private Funding createFunding(
            BigDecimal amount,
            Currency currency
    ) {

        return Funding.builder()
                .user(user)
                .amount(amount)
                .currency(currency)
                .reference("FND-TEST-001")
                .provider(FundingProvider.PAYSTACK)
                .build();
    }
}
