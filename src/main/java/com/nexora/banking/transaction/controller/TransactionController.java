package com.nexora.banking.transaction.controller;

import com.nexora.banking.common.response.PageResponse;
import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.dto.response.TransactionResponse;
import com.nexora.banking.transaction.validator.PaginationValidator;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final PaginationValidator paginationValidator;

    @GetMapping
    public PageResponse<TransactionResponse> getTransactions(
        @AuthenticationPrincipal User currentUser,
        @ModelAttribute TransactionFilterRequest filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        
        paginationValidator.validate(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        
        Page<TransactionResponse> transactions =
                transactionService.getTransactions(
                        currentUser,
                        filter,
                        pageable
                );

        return PageResponse.from(transactions);
    }
}
