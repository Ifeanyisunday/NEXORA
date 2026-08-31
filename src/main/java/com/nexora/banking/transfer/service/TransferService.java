package com.nexora.banking.transfer.service;

import com.nexora.banking.transfer.dto.request.TransferRequest;
import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.user.entity.User;

public interface TransferService {

    TransferResponse transfer(
            User sender,
            TransferRequest request,
            String idempotencyKey
    );
}