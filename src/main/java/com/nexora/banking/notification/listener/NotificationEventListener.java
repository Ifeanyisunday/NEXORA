package com.nexora.banking.notification.listener;

import com.nexora.banking.notification.service.NotificationService;
import com.nexora.banking.transfer.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTransferCompleted(
            TransferCompletedEvent event
    ) {

        log.info(
                "TransferCompletedEvent received AFTER_COMMIT. transferId={}, senderId={}, receiverId={}, amount={}",
                event.transferId(),
                event.senderId(),
                event.receiverId(),
                event.amount()
        );
        
        notificationService.createTransferNotifications(
                event
        );
    }
}