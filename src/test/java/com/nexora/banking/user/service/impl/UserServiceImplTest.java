package com.nexora.banking.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.nexora.banking.common.exception.EmailAlreadyExistsException;
import com.nexora.banking.user.dto.request.RegisterUserRequest;
import com.nexora.banking.user.dto.response.UserResponse;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.auth.enums.UserRole;
import com.nexora.banking.user.enums.UserStatus;
import com.nexora.banking.user.mapper.UserMapper;
import com.nexora.banking.user.repository.UserRepository;
import com.nexora.banking.user.service.impl.UserServiceImpl;
import com.nexora.banking.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterUserRequest request;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        request = new RegisterUserRequest(
                "ifeanyi@example.com",
                "password123"
        );

        user = new User();
        user.setEmail(request.email());

        userResponse = new UserResponse(
                UUID.randomUUID(),
                request.email(),
                UserStatus.PENDING_VERIFICATION,
                false
        );
    }

    @Test
    void register_shouldCreateUserAndWalletSuccessfully() {

        // Arrange
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        // Act
        UserResponse result = userService.register(request);

        // Assert
        assertThat(result)
                .isEqualTo(userResponse);

        assertThat(user.getPasswordHash())
                .isEqualTo("hashed-password");

        assertThat(user.getRole())
                .isEqualTo(UserRole.CUSTOMER);

        assertThat(user.getStatus())
                .isEqualTo(UserStatus.PENDING_VERIFICATION);

        assertThat(user.isEmailVerified())
                .isFalse();

        assertThat(user.getFailedLoginAttempts())
                .isZero();

        // Verify interactions
        verify(userRepository)
                .existsByEmail(request.email());

        verify(userMapper)
                .toEntity(request);

        verify(passwordEncoder)
                .encode(request.password());

        verify(userRepository)
                .save(user);

        verify(walletService)
                .createWallet(user);

        verify(userMapper)
                .toResponse(user);
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(
                () -> userService.register(request)
        )
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage(
                        "Email already exists: " + request.email()
                );

        // Verify
        verify(userRepository)
                .existsByEmail(request.email());

        // Nothing after the duplicate check should execute
        verifyNoInteractions(
                userMapper,
                passwordEncoder,
                walletService
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void register_shouldHashPasswordBeforeSavingUser() {

        // Arrange
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        // Act
        userService.register(request);

        // Assert
        verify(passwordEncoder)
                .encode(request.password());

        assertThat(user.getPasswordHash())
                .isEqualTo("hashed-password");

        verify(userRepository)
                .save(user);
    }

    @Test
    void register_shouldCreateWalletForSavedUser() {

        // Arrange
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        // Act
        userService.register(request);

        // Assert
        verify(walletService)
                .createWallet(user);
    }

    @Test
    void register_shouldReturnMappedUserResponse() {

        // Arrange
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        // Act
        UserResponse result =
                userService.register(request);

        // Assert
        assertThat(result)
                .isSameAs(userResponse);

        verify(userMapper)
                .toResponse(user);
    }
}