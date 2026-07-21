package com.nexora.banking.wallet.service;

import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.factory.WalletFactory;
import com.nexora.banking.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public Wallet createWallet(User user) {
        Wallet wallet = WalletFactory.create(user);
        return walletRepository.save(wallet);
    }

}