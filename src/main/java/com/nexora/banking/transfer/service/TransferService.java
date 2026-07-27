package com.nexora.banking.transfer.service;

import com.nexora.banking.common.exception.IdempotencyKeyConflictException;
import com.nexora.banking.transfer.dto.request.TransferRequest;
import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.transfer.enums.TransferStatus;
import com.nexora.banking.transfer.mapper.TransferMapper;
import com.nexora.banking.transfer.repository.TransferRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.banking.common.exception.SelfTransferException;
import com.nexora.banking.common.exception.WalletNotActiveException;
import com.nexora.banking.common.exception.WalletNotFoundException;

import java.util.UUID;
import java.time.Instant;
import java.util.Optional;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;

    private final TransferRepository transferRepository;

    private final TransferMapper transferMapper;


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
        UUID receiverId = request.receiverId();

        // 2. Prevent self-transfer

        if (senderId.equals(receiverId)) {
            throw new SelfTransferException(
                    "You cannot transfer money to yourself."
            );
        }

        // 3. First idempotency check before locking wallets

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
                                "The provided idempotency key has already been used for a different transfer."
                        );
                }
                return transferMapper.toResponse(
                        transfer
                );
        }


        // 4. Determine consistent lock order

        UUID firstUserId;
        UUID secondUserId;

        if (senderId.compareTo(receiverId) < 0) {
            firstUserId = senderId;
            secondUserId = receiverId;
        } else {
            firstUserId = receiverId;
            secondUserId = senderId;
        }


        // 5. Lock first wallet

        Wallet firstWallet =
                walletRepository.findByUserIdForUpdate(
                        firstUserId
                ).orElseThrow(
                        () -> new WalletNotFoundException(
                                "Wallet not found."
                        )
                );


        // 6. Lock second wallet

        Wallet secondWallet =
                walletRepository.findByUserIdForUpdate(
                        secondUserId
                ).orElseThrow(
                        () -> new WalletNotFoundException(
                                "Wallet not found."
                        )
                );

        // 7. Second idempotency check after locking wallets
        // Protects against concurrent duplicate requests

        Optional<Transfer> transferAfterLock =
                transferRepository.findByIdempotencyKey(
                        idempotencyKey
                );

        if (transferAfterLock.isPresent()) {
                Transfer transfer = transferAfterLock.get();

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
                                "The Idempotency-Key has already been used " +
                                "for a different transfer."
                        );
                }
                return transferMapper.toResponse(
                        transfer
                );
        }

        // 8. Identify sender and receiver wallets

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


        // 9. Validate sender wallet status

        if (
                senderWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {
             throw new WalletNotActiveException(
                    "Sender wallet is not active."
            );
        }

        // 10. Validate receiver wallet status

        if (
                receiverWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {
            throw new WalletNotActiveException(
                    "Receiver wallet is not active."
            );
        }


        // 11. Debit sender

        senderWallet.withdraw(
                request.amount()
        );


        // 12. Credit receiver

        receiverWallet.deposit(
                request.amount()
        );


        // 13. Create transfer record

        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiverWallet.getUser())
                .amount(request.amount())
                .status(TransferStatus.COMPLETED)
                .idempotencyKey(idempotencyKey)
                .reference(generateReference())
                .completedAt(Instant.now())
                .build();


        // 14. Save transfer

        Transfer savedTransfer =
                transferRepository.save(
                        transfer
                );


        // 15. Return response

        return transferMapper.toResponse(
                savedTransfer
        );

    }

    private String generateReference() {
        return "TRX-" + UUID.randomUUID();
    }

}