package com.nexora.banking.transfer.service;

import com.nexora.banking.transfer.dto.request.TransferRequest;
import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.transfer.entity.Transfer;
import com.nexora.banking.transfer.entity.TransferStatus;
import com.nexora.banking.transfer.mapper.TransferMapper;
import com.nexora.banking.transfer.repository.TransferRepository;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.entity.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

            TransferRequest request

    ) {

        UUID senderId = sender.getId();

        UUID receiverId = request.receiverId();


        // 1. Prevent self-transfer

        if (senderId.equals(receiverId)) {

            throw new IllegalArgumentException(
                    "You cannot transfer money to yourself."
            );

        }


        // 2. Determine consistent lock order

        UUID firstUserId;

        UUID secondUserId;


        if (senderId.compareTo(receiverId) < 0) {

            firstUserId = senderId;

            secondUserId = receiverId;

        } else {

            firstUserId = receiverId;

            secondUserId = senderId;

        }


        // 3. Lock first wallet

        Wallet firstWallet =

                walletRepository.findByIdForUpdate(
                        firstUserId
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "Wallet not found."
                        )
                );


        // 4. Lock second wallet

        Wallet secondWallet =

                walletRepository.findByIdForUpdate(
                        secondUserId
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "Wallet not found."
                        )
                );


        // 5. Identify sender and receiver wallets

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


        // 6. Check sender wallet status

        if (
                senderWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {

            throw new IllegalStateException(
                    "Sender wallet is not active."
            );

        }


        // 7. Check receiver wallet status

        if (
                receiverWallet.getStatus()
                        != WalletStatus.ACTIVE
        ) {

            throw new IllegalStateException(
                    "Receiver wallet is not active."
            );

        }


        // 8. Withdraw from sender

        senderWallet.withdraw(
                request.amount()
        );


        // 9. Deposit into receiver

        receiverWallet.deposit(
                request.amount()
        );


        // 10. Create transfer record

        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(
                        receiverWallet.getUser()
                )
                .amount(
                        request.amount()
                )
                .status(
                        TransferStatus.COMPLETED
                )
                .build();


        // 11. Save transfer

        Transfer savedTransfer =

                transferRepository.save(
                        transfer
                );


        // 12. Return response

        return transferMapper.toResponse(
                savedTransfer
        );

    }

}