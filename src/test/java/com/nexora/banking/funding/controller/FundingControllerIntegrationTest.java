
package com.nexora.banking.funding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.banking.auth.jwt.JwtService;
import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.enums.FundingMethod;
import com.nexora.banking.funding.enums.FundingProvider;
import com.nexora.banking.funding.enums.FundingStatus;
import com.nexora.banking.funding.service.FundingService;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.user.repository.UserRepository;
import com.nexora.banking.wallet.enums.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FundingControllerIntegrationTest {

    private static final String FUNDING_URL = "/api/v1/fundings";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FundingService fundingService;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {

        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("funding-controller-" + uniqueId + "@example.com");
        user.setPasswordHash("integration-test-hash");

        testUser = userRepository.saveAndFlush(user);

        jwtToken = jwtService.generateToken(testUser);
    }

    @Test
    void initiateFunding_shouldReturnCreatedForAuthenticatedUser()
            throws Exception {

        FundingResponse response = new FundingResponse(
                UUID.randomUUID(),
                "FND-TEST-123",
                new BigDecimal("500.00"),
                Currency.NGN,
                FundingMethod.CARD,
                FundingProvider.MOCK,
                FundingStatus.PROCESSING,
                "MOCK-123",
                Instant.now(),
                null
        );

        when(fundingService.initiateFunding(
                any(User.class),
                any(CreateFundingRequest.class),
                eq("funding-request-123")
        )).thenReturn(response);

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "funding-request-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("FND-TEST-123"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.currency").value("NGN"))
                .andExpect(jsonPath("$.method").value("CARD"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(fundingService, times(1)).initiateFunding(
                any(User.class),
                any(CreateFundingRequest.class),
                eq("funding-request-123")
        );
    }

    @Test
    void initiateFunding_shouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Idempotency-Key", "funding-request-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectMissingIdempotencyKey()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectBlankIdempotencyKey()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectIdempotencyKeyLongerThan100Characters()
            throws Exception {

        String longKey = "A".repeat(101);

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", longKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectZeroAmount()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "funding-zero-amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 0,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectNegativeAmount()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "funding-negative-amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": -100,
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectMissingFundingMethod()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "funding-missing-method")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500.00
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }

    @Test
    void initiateFunding_shouldRejectMissingAmount()
            throws Exception {

        mockMvc.perform(post(FUNDING_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Idempotency-Key", "funding-missing-amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "method": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundingService);
    }
}
