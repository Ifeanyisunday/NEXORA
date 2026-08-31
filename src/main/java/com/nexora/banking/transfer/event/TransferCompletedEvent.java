package com.nexora.banking.transfer.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCompletedEvent(

        UUID transferId,

        UUID senderId,

        UUID receiverId,

        BigDecimal amount
) {
}