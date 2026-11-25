package com.careerpilot.auth.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careerpilot.auth.dto.LoginRequest;
import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.auth.dto.UserResponse;
import com.careerpilot.auth.entity.User;
import com.careerpilot.auth.enums.Role;
import com.careerpilot.auth.enums.UserStatus;
import com.careerpilot.auth.exception.EmailAlreadyExistsException;
import com.careerpilot.auth.exception.InvalidCredentialsException;
import com.careerpilot.auth.mapper.UserMapper;
import com.careerpilot.auth.repository.UserRepository;
import com.careerpilot.auth.security.JwtService;
import com.careerpilot.auth.service.AuthService;
import com.careerpilot.auth.service.RefreshTokenService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public UserResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        User user = userMapper.toEntity(request);
        user.setEmail(email);
        user.setId(UUID.randomUUID());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                loggedInUser.getPasswordHash())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (loggedInUser.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken =
                jwtService.generateAccessToken(loggedInUser.getId().toString());

        String refreshToken =
                refreshTokenService.createRefreshToken(loggedInUser);

        return userMapper.toLoginResponse(
                loggedInUser,
                accessToken,
                refreshToken
        );
    }

}
