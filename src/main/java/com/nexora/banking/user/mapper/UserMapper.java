package com.nexora.banking.user.mapper;

import com.nexora.banking.user.dto.request.RegisterUserRequest;
import com.nexora.banking.user.dto.response.UserResponse;
import com.nexora.banking.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)

    // inherited from BaseEntity
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "authorities", ignore = true)

    
    User toEntity(RegisterUserRequest request);

    UserResponse toResponse(User user);

}