package com.nexora.banking.auth.dto.response;

public record LoginResponse(

        String accessToken,

        String tokenType,

        long expiresIn,

        String email,

        String role

) {
}