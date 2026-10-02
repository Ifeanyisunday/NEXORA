package com.nexora.banking.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.banking.common.exception.InsufficientBalanceException;
import com.nexora.banking.wallet.exceptions.WalletNotFoundException;
import com.nexora.banking.transaction.entity.Transaction;
import com.nexora.banking.transaction.enums.TransactionCategory;
import com.nexora.banking.transaction.enums.TransactionType;
import com.nexora.banking.transaction.service.TransactionService;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.wallet.dto.response.WalletResponse;
import com.nexora.banking.wallet.entity.Wallet;
import com.nexora.banking.wallet.enums.Currency;
import com.nexora.banking.wallet.enums.WalletStatus;
import com.nexora.banking.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;
    private UUID userId;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = new User();

        user.setEmail(
                "ifeanyi@example.com"
        );

        user.setId(userId);

        wallet = Wallet.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.NGN)
                .status(WalletStatus.ACTIVE)
                .build();
    }

    @Test
    void getMyWallet_shouldReturnWallet() {

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                walletService.getMyWallet(userId);

        assertThat(response)
                .isNotNull();

        assertThat(response.userId())
                .isEqualTo(userId);

        assertThat(response.balance())
                .isEqualByComparingTo("1000.00");

        assertThat(response.currency())
                .isEqualTo(Currency.NGN);

        assertThat(response.status())
                .isEqualTo(WalletStatus.ACTIVE);

        verify(walletRepository)
                .findByUserId(userId);
    }

    @Test
    void getMyWallet_shouldThrowWhenWalletDoesNotExist() {

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> walletService.getMyWallet(userId)
        )
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Wallet not found.");

        verify(walletRepository)
                .findByUserId(userId);
    }

    @Test
    void deposit_shouldIncreaseWalletBalanceAndCreateLedgerTransaction() {

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                walletService.deposit(
                        userId,
                        new BigDecimal("500.00")
                );

        // Wallet state
        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1500.00");

        assertThat(response.balance())
                .isEqualByComparingTo("1500.00");

        // Repository interaction
        verify(walletRepository)
                .findByUserIdForUpdate(userId);

        // Ledger transaction
        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionService)
                .save(transactionCaptor.capture());

        Transaction transaction =
                transactionCaptor.getValue();

        assertThat(transaction.getType())
                .isEqualTo(TransactionType.CREDIT);

        assertThat(transaction.getCategory())
                .isEqualTo(TransactionCategory.DEPOSIT);

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("500.00");

        assertThat(transaction.getBalanceBefore())
                .isEqualByComparingTo("1000.00");

        assertThat(transaction.getBalanceAfter())
                .isEqualByComparingTo("1500.00");

        assertThat(transaction.getDescription())
                .isEqualTo("Wallet deposit");

        assertThat(transaction.getTransfer())
                .isNull();

        assertThat(transaction.getWallet())
                .isEqualTo(wallet);
    }

    @Test
    void withdraw_shouldDecreaseWalletBalanceAndCreateLedgerTransaction() {

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                walletService.withdraw(
                        userId,
                        new BigDecimal("300.00")
                );

        // Wallet state
        assertThat(wallet.getBalance())
                .isEqualByComparingTo("700.00");

        assertThat(response.balance())
                .isEqualByComparingTo("700.00");

        // Repository interaction
        verify(walletRepository)
                .findByUserIdForUpdate(userId);

        // Ledger transaction
        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionService)
                .save(transactionCaptor.capture());

        Transaction transaction =
                transactionCaptor.getValue();

        assertThat(transaction.getType())
                .isEqualTo(TransactionType.DEBIT);

        assertThat(transaction.getCategory())
                .isEqualTo(TransactionCategory.WITHDRAWAL);

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("300.00");

        assertThat(transaction.getBalanceBefore())
                .isEqualByComparingTo("1000.00");

        assertThat(transaction.getBalanceAfter())
                .isEqualByComparingTo("700.00");

        assertThat(transaction.getDescription())
                .isEqualTo("Wallet withdrawal");

        assertThat(transaction.getTransfer())
                .isNull();

        assertThat(transaction.getWallet())
                .isEqualTo(wallet);
    }

    @Test
    void withdraw_shouldRejectInsufficientBalanceAndNotCreateLedgerTransaction() {

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        assertThatThrownBy(
                () -> walletService.withdraw(
                        userId,
                        new BigDecimal("1500.00")
                )
        )
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance.");

        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verify(walletRepository)
                .findByUserIdForUpdate(userId);

        verify(transactionService, never())
                .save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrowWhenWalletDoesNotExist() {

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> walletService.deposit(
                        userId,
                        new BigDecimal("500.00")
                )
        )
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Wallet not found.");

        verify(transactionService, never())
                .save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldThrowWhenWalletDoesNotExist() {

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> walletService.withdraw(
                        userId,
                        new BigDecimal("500.00")
                )
        )
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessage("Wallet not found.");

        verify(transactionService, never())
                .save(any(Transaction.class));
    }
}