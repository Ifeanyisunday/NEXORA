package com.nexora.banking.transaction.controller;

import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.dto.response.TransactionResponse;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public Page<TransactionResponse> getTransactions(

            @AuthenticationPrincipal
            User currentUser,

            @ModelAttribute
            TransactionFilterRequest filter,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable

    ) {

        return transactionService.getTransactions(
                currentUser,
                filter,
                pageable
        );

    }

}