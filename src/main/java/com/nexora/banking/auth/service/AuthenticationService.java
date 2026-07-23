package com.nexora.banking.auth.service;

import com.nexora.banking.auth.config.JwtProperties;
import com.nexora.banking.auth.dto.request.LoginRequest;
import com.nexora.banking.auth.dto.response.LoginResponse;
import com.nexora.banking.auth.jwt.JwtService;
import com.nexora.banking.common.exception.InvalidCredentialsException;
import com.nexora.banking.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {

         try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email(),
                                    request.password()
                            )
                    );

            User user = (User) authentication.getPrincipal();

            String token = jwtService.generateToken(user);

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtProperties.getExpiration().toSeconds(),
                    user.getEmail(),
                    user.getRole().name()
            );

        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();

        }
    }

}