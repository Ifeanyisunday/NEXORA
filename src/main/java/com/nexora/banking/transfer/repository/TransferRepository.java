package com.nexora.banking.transfer.repository;

import com.nexora.banking.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface TransferRepository
        extends JpaRepository<Transfer, UUID> {

    List<Transfer> findBySenderIdOrderByCreatedAtDesc(
            UUID senderId
    );

    List<Transfer> findByReceiverIdOrderByCreatedAtDesc(
            UUID receiverId
    );

    Optional<Transfer> findByIdempotencyKey(
            String idempotencyKey
    );

    Optional<Transfer> findByReference(
            String reference
    );

}