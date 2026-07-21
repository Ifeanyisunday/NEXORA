package com.nexora.banking.transfer.repository;

import com.nexora.banking.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRepository
        extends JpaRepository<Transfer, UUID> {

    List<Transfer> findBySenderIdOrderByCreatedAtDesc(
            UUID senderId
    );

    List<Transfer> findByReceiverIdOrderByCreatedAtDesc(
            UUID receiverId
    );

}