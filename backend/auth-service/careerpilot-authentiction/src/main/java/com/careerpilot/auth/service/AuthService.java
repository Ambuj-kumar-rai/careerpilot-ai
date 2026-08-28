package com.careerpilot.auth.service;

import com.careerpilot.auth.dto.LoginRequest;
import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.auth.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
