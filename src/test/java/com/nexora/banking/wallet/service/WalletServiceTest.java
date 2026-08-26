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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.banking.common.exception.ResourceNotFoundException;
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

        // Assuming BaseEntity has a setId method.
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

        // Arrange
        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        // Act
        WalletResponse response =
                walletService.getMyWallet(userId);

        // Assert
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

        // Arrange
        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(
                () -> walletService.getMyWallet(userId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wallet not found.");

        verify(walletRepository)
                .findByUserId(userId);
    }

    @Test
    void deposit_shouldIncreaseWalletBalance() {

        // Arrange
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        // Act
        WalletResponse response =
                walletService.deposit(
                        userId,
                        new BigDecimal("500.00")
                );

        // Assert
        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1500.00");

        assertThat(response.balance())
                .isEqualByComparingTo("1500.00");

        verify(walletRepository)
                .findByUserIdForUpdate(userId);
    }

    @Test
    void withdraw_shouldDecreaseWalletBalance() {

        // Arrange
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        // Act
        WalletResponse response =
                walletService.withdraw(
                        userId,
                        new BigDecimal("300.00")
                );

        // Assert
        assertThat(wallet.getBalance())
                .isEqualByComparingTo("700.00");

        assertThat(response.balance())
                .isEqualByComparingTo("700.00");

        verify(walletRepository)
                .findByUserIdForUpdate(userId);
    }

    @Test
    void withdraw_shouldRejectInsufficientBalance() {

        // Arrange
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        // Act + Assert
        assertThatThrownBy(
                () -> walletService.withdraw(
                        userId,
                        new BigDecimal("1500.00")
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient balance.");

        // Balance must remain unchanged
        assertThat(wallet.getBalance())
                .isEqualByComparingTo("1000.00");

        verify(walletRepository)
                .findByUserIdForUpdate(userId);
    }

    @Test
    void deposit_shouldThrowWhenWalletDoesNotExist() {

        // Arrange
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(
                () -> walletService.deposit(
                        userId,
                        new BigDecimal("500.00")
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wallet not found.");
    }

    @Test
    void withdraw_shouldThrowWhenWalletDoesNotExist() {

        // Arrange
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(
                () -> walletService.withdraw(
                        userId,
                        new BigDecimal("500.00")
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Wallet not found.");
    }
}