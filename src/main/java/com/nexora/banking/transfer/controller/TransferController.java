package com.nexora.banking.transfer.controller;

import com.nexora.banking.transfer.dto.request.TransferRequest;
import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.transfer.service.TransferService;
import com.nexora.banking.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(

            @AuthenticationPrincipal
            User currentUser,

            @Valid
            @RequestBody
            TransferRequest request

    ) {

        return transferService.transfer(
                currentUser,
                request
        );

    }

}