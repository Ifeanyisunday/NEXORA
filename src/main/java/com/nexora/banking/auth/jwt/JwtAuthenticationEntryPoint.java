package com.nexora.banking.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.banking.common.constants.ErrorCode;
import com.nexora.banking.common.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(

            HttpServletRequest request,

            HttpServletResponse response,

            AuthenticationException authException

    ) throws IOException, ServletException {

        ApiErrorResponse error = new ApiErrorResponse(

                Instant.now(),

                HttpStatus.UNAUTHORIZED.value(),

                HttpStatus.UNAUTHORIZED.getReasonPhrase(),

                ErrorCode.UNAUTHORIZED.name(),

                "Authentication is required.",

                request.getRequestURI()

        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );

    }

}