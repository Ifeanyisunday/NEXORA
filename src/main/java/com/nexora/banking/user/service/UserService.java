package com.nexora.banking.user.service;

import com.nexora.banking.user.dto.request.RegisterUserRequest;
import com.nexora.banking.user.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);

}