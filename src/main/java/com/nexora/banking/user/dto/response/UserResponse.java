package com.nexora.banking.user.dto.response;

import com.nexora.banking.user.enums.UserStatus;

import java.util.UUID;

public record UserResponse(

        UUID id,
        String email,
        UserStatus status,
        boolean emailVerified

) {
}