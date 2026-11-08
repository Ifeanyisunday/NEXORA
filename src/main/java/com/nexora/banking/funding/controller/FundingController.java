package com.nexora.banking.funding.controller;

import com.nexora.banking.funding.dto.request.CreateFundingRequest;
import com.nexora.banking.funding.dto.response.FundingResponse;
import com.nexora.banking.funding.service.FundingService;
import com.nexora.banking.user.entity.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fundings")
@RequiredArgsConstructor
@Validated
public class FundingController {

    private final FundingService fundingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FundingResponse initiateFunding(

            @AuthenticationPrincipal
            User currentUser,

            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key header is required")
            @Size(
                    max = 100,
                    message = "Idempotency-Key must not exceed 100 characters"
            )
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateFundingRequest request

    ) {

        return fundingService.initiateFunding(
                currentUser,
                request,
                idempotencyKey
        );
    }
}