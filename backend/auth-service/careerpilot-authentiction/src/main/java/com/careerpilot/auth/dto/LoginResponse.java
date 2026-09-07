package com.careerpilot.auth.dto;

import java.util.UUID;

import com.careerpilot.auth.enums.Role;

public record LoginResponse(
    UUID id,
    String name,
    String email,
    Role role,
    String accessToken
) {
    
}
