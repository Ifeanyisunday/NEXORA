package com.nexora.banking.transfer.dto.response;

import com.nexora.banking.transfer.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(

        UUID transferId,

        UUID senderId,

        UUID receiverId,

        BigDecimal amount,

        TransferStatus status,

        Instant createdAt

) {
}