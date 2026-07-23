package com.nexora.banking.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nexora.banking.auth.dto.request.LoginRequest;
import com.nexora.banking.auth.dto.response.LoginResponse;
import com.nexora.banking.auth.jwt.JwtService;
import com.nexora.banking.auth.enums.UserRole;
import com.nexora.banking.user.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequest request;
    private User user;

    private static final String JWT_TOKEN =
            "test.jwt.token";

    private static final long JWT_EXPIRATION =
            900_000L;

    @BeforeEach
    void setUp() {

        request = new LoginRequest(
                "ifeanyi@example.com",
                "password123"
        );

        user = new User();

        user.setEmail(
                "ifeanyi@example.com"
        );

        user.setRole(
                UserRole.CUSTOMER
        );
    }

    @Test
    void login_shouldAuthenticateUserAndReturnJwt() {

        // Arrange
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(JWT_TOKEN);

        // Act
        LoginResponse response =
                authenticationService.login(request);

        // Assert
        assertThat(response)
                .isNotNull();

        assertThat(response.accessToken())
                .isEqualTo(JWT_TOKEN);

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.expiresIn())
                .isEqualTo(900);

        assertThat(response.email())
                .isEqualTo("ifeanyi@example.com");

        assertThat(response.role())
                .isEqualTo("CUSTOMER");
    }

    @Test
    void login_shouldAuthenticateUsingEmailAndPassword() {

        // Arrange
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(JWT_TOKEN);

        // Act
        authenticationService.login(request);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken>
                captor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager)
                .authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token =
                captor.getValue();

        assertThat(token.getPrincipal())
                .isEqualTo(request.email());

        assertThat(token.getCredentials())
                .isEqualTo(request.password());
    }

    @Test
    void login_shouldGenerateJwtForAuthenticatedUser() {

        // Arrange
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(JWT_TOKEN);

        // Act
        authenticationService.login(request);

        // Assert
        verify(jwtService)
                .generateToken(user);
    }

    @Test
    void login_shouldReturnAuthenticatedUserInformation() {

        // Arrange
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(JWT_TOKEN);

        // Act
        LoginResponse response =
                authenticationService.login(request);

        // Assert
        assertThat(response.email())
                .isEqualTo(user.getEmail());

        assertThat(response.role())
                .isEqualTo(user.getRole().name());
    }

    @Test
    void login_shouldPropagateAuthenticationFailure() {

        // Arrange
        var authenticationException =
                new org.springframework.security.authentication.BadCredentialsException(
                        "Bad credentials"
                );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(authenticationException);

        // Act + Assert
        org.assertj.core.api.Assertions
                .assertThatThrownBy(
                        () -> authenticationService.login(request)
                )
                .isSameAs(authenticationException);

        // JWT must never be generated
        verifyNoInteractions(jwtService);
    }
}