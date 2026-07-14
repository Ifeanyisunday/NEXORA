package com.nexora.banking.auth.controller;

import com.nexora.banking.user.dto.request.RegisterUserRequest;
import com.nexora.banking.user.dto.response.UserResponse;
import com.nexora.banking.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody RegisterUserRequest requesst
    ){
        UserResponse response = userService.register(requesst);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}