package com.nexora.banking.user.service.impl;

import com.nexora.banking.user.dto.request.RegisterUserRequest;
import com.nexora.banking.user.dto.response.UserResponse;
import com.nexora.banking.user.entity.User;
import com.nexora.banking.user.enums.UserStatus;
import com.nexora.banking.common.exception.EmailAlreadyExistsException;
import com.nexora.banking.user.mapper.UserMapper;
import com.nexora.banking.user.repository.UserRepository;
import com.nexora.banking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexora.banking.wallet.service.WalletService;


@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;


    @Override
    public UserResponse register(RegisterUserRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException(request.email()) ;
        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(
            passwordEncoder.encode(request.password())
        );

        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(user);

        walletService.createWallet(savedUser);

        return userMapper.toResponse(savedUser);


    }
}