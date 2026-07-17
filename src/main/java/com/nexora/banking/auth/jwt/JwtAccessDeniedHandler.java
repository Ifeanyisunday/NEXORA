package com.nexora.banking.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.banking.common.constants.ErrorCode;
import com.nexora.banking.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(

            HttpServletRequest request,

            HttpServletResponse response,

            AccessDeniedException accessDeniedException

    ) throws IOException {

        ApiErrorResponse error = new ApiErrorResponse(

                Instant.now(),

                HttpStatus.FORBIDDEN.value(),

                HttpStatus.FORBIDDEN.getReasonPhrase(),

                ErrorCode.ACCESS_DENIED.name(),

                "You do not have permission to perform this action.",

                request.getRequestURI()

        );

        response.setStatus(HttpStatus.FORBIDDEN.value());

        response.setContentType("application/json");

        objectMapper.writeValue(

                response.getOutputStream(),

                error

        );

    }

}