package com.nexora.banking.transaction.service;


import com.nexora.banking.user.entity.User; 

import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.dto.response.TransactionResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    
    Transaction save(Transaction transaction);

    Page<TransactionResponse> getTransactions(
        User currentUser,
        TransactionFilterRequest filter,
        Pageable pageable
    );
}
