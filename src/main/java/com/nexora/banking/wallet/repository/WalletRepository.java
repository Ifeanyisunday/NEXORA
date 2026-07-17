package com.nexora.banking.wallet.repository;

import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository
        extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(UUID userId);

}