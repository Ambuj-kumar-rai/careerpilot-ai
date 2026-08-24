package com.careerpilot.auth.dto;

import java.util.UUID;

import com.careerpilot.auth.enums.Role;
import com.careerpilot.auth.enums.UserStatus;

public record UserResponse(UUID id, String name, String email, Role role, UserStatus status) {
    
}
