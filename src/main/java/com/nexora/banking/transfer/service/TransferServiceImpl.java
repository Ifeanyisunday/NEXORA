package com.nexora.banking.transfer.service;

import com.nexora.banking.common.exception.IdempotencyKeyConflictException;
import com.nexora.banking.common.exception.SelfTransferException;
import com.nexora.banking.common.exception.WalletNotActiveException;
import com.nexora.banking.common.exception.WalletNotFoundException;

import com.nexora.banking.transfer.dto.request.TransferRequest;
import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.transfer.enums.TransferStatus;
import com.nexora.banking.transfer.event.TransferCompletedEvent;
import com.nexora.banking.transfer.mapper.TransferMapper;
import com.nexora.banking.transfer.repository.TransferRepository;

import com.nexora.banking.user.entity.User;

import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;

import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.factory.TransactionFactory;
import com.nexora.banking.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransferResponse transfer(
            User sender,
            TransferRequest request,
            String idempotencyKey
    ) {

        // 1. Validate idempotency key

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required."
            );
        }

        UUID senderId = sender.getId();

        // 2. Find destination wallet

        Wallet destinationWallet = walletRepository
                .findByAccountNumber(
                        request.accountNumber()
                )
                .orElseThrow(
                        () -> new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        UUID receiverId =
                destinationWallet.getUser().getId();

        // 3. Prevent self-transfer

        if (senderId.equals(receiverId)) {
            throw new SelfTransferException(
                    "You cannot transfer money to yourself."
            );
        }

        // 4. First idempotency check

        Optional<Transfer> existingTransfer =
                transferRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (existingTransfer.isPresent()) {

            Transfer transfer = existingTransfer.get();

            boolean sameRequest =
                    transfer.getSender()
                            .getId()
                            .equals(senderId)
                    && transfer.getReceiver()
                            .getId()
                            .equals(receiverId)
                    && transfer.getAmount()
                            .compareTo(request.amount()) == 0;

            if (!sameRequest) {
                throw new IdempotencyKeyConflictException(
                        "The provided idempotency key has already been used "
                                + "for a different transfer."
                );
            }

            return transferMapper.toResponse(
                    transfer
            );
        }

        // 5. Determine consistent lock order

        UUID firstUserId;
        UUID secondUserId;

        if (senderId.compareTo(receiverId) < 0) {

            firstUserId = senderId;
            secondUserId = receiverId;

        } else {

            firstUserId = receiverId;
            secondUserId = senderId;
        }

        // 6. Lock first wallet

        Wallet firstWallet =
                walletRepository
                        .findByUserIdForUpdate(
                                firstUserId
                        )
                        .orElseThrow(
                                () -> new WalletNotFoundException(
                                        "Wallet not found."
                                )
                        );

        // 7. Lock second wallet

        Wallet secondWallet =
                walletRepository
                        .findByUserIdForUpdate(
                                secondUserId
                        )
                        .orElseThrow(
                                () -> new WalletNotFoundException(
                                        "Wallet not found."
                                )
                        );

        // 8. Second idempotency check

        Optional<Transfer> transferAfterLock =
                transferRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (transferAfterLock.isPresent()) {

            Transfer transfer =
                    transferAfterLock.get();

            boolean sameRequest =
                    transfer.getSender()
                            .getId()
                            .equals(senderId)
                    && transfer.getReceiver()
                            .getId()
                            .equals(receiverId)
                    && transfer.getAmount()
                            .compareTo(request.amount()) == 0;

            if (!sameRequest) {
                throw new IdempotencyKeyConflictException(
                        "The Idempotency-Key has already been used "
                                + "for a different transfer."
                );
            }

            return transferMapper.toResponse(
                    transfer
            );
        }

        // 9. Identify sender and receiver wallets

        Wallet senderWallet;
        Wallet receiverWallet;

        if (
                firstWallet.getUser()
                        .getId()
                        .equals(senderId)
        ) {

            senderWallet = firstWallet;
            receiverWallet = secondWallet;

        } else {

            senderWallet = secondWallet;
            receiverWallet = firstWallet;
        }

        // 10. Validate sender wallet

        if (
                senderWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {

            throw new WalletNotActiveException(
                    "Sender wallet is not active."
            );
        }

        // 11. Validate receiver wallet

        if (
                receiverWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {

            throw new WalletNotActiveException(
                    "Receiver wallet is not active."
            );
        }

        // 12. Store balances before transfer

        BigDecimal senderBalanceBefore =
                senderWallet.getBalance();

        BigDecimal receiverBalanceBefore =
                receiverWallet.getBalance();

        // 13. Debit sender

        senderWallet.withdraw(
                request.amount()
        );

        // 14. Credit receiver

        receiverWallet.deposit(
                request.amount()
        );

        // 15. Store balances after transfer

        BigDecimal senderBalanceAfter =
                senderWallet.getBalance();

        BigDecimal receiverBalanceAfter =
                receiverWallet.getBalance();

        // 16. Create transfer record

        Transfer transfer =
                Transfer.builder()
                        .sender(sender)
                        .receiver(receiverWallet.getUser())
                        .amount(request.amount())
                        .status(TransferStatus.COMPLETED)
                        .idempotencyKey(idempotencyKey)
                        .reference(generateReference())
                        .completedAt(Instant.now())
                        .build();

        // 17. Save transfer

        Transfer savedTransfer =
                transferRepository.save(
                        transfer
                );

        // 18. Create sender transaction record

        Transaction senderTransaction =
                TransactionFactory.create(
                        senderWallet,
                        savedTransfer,
                        TransactionType.DEBIT,
                        senderBalanceBefore,
                        senderBalanceAfter,
                        "Transfer to "
                                + receiverWallet.getUser()
                                .getUsername()
                );

        transactionService.save(
                senderTransaction
        );

        // 19. Create receiver transaction record

        Transaction receiverTransaction =
                TransactionFactory.create(
                        receiverWallet,
                        savedTransfer,
                        TransactionType.CREDIT,
                        receiverBalanceBefore,
                        receiverBalanceAfter,
                        "Transfer from "
                                + sender.getUsername()
                );

        transactionService.save(
                receiverTransaction
        );

        // 20. Publish event
        //
        // The listener will NOT execute immediately.
        // Because it uses AFTER_COMMIT, it will execute
        // only after this database transaction succeeds.

        eventPublisher.publishEvent(
                new TransferCompletedEvent(
                        savedTransfer.getId(),
                        senderId,
                        receiverId,
                        savedTransfer.getAmount()
                )
        );

        // 21. Return response

        return transferMapper.toResponse(
                savedTransfer
        );
    }

    private String generateReference() {

        return "TRX-" + UUID.randomUUID();
    }
}