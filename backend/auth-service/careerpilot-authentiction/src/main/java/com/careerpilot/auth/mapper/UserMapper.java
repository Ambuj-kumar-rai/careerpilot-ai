package com.careerpilot.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.auth.dto.UserResponse;
import com.careerpilot.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)

    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

    @Mapping(target = "accessToken", source = "accessToken")
    LoginResponse toLoginResponse(User user, String accessToken);
    
}
