package com.nexora.banking.transaction.service;

import com.nexora.banking.common.exception.WalletNotFoundException;
import com.nexora.banking.transaction.dto.request.TransactionFilterRequest;
import com.nexora.banking.transaction.dto.response.TransactionResponse;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.mapper.TransactionMapper;
import com.nexora.banking.transaction.repository.TransactionRepository;
import com.nexora.banking.transaction.specification.TransactionSpecification;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }


    public Page<TransactionResponse> getTransactions(

            User currentUser,
            TransactionFilterRequest filter,
            Pageable pageable

    ) {

        Wallet wallet = walletRepository
                .findByUser(currentUser)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        return transactionRepository.findAll(
                TransactionSpecification.filter(
                        wallet.getId(),
                        filter
                ),
                pageable
            ).map(transactionMapper::toResponse);

    }


}