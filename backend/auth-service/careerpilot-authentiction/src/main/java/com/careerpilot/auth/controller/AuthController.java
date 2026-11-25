package com.careerpilot.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.careerpilot.auth.dto.RefreshTokenRequest;
import com.careerpilot.auth.service.RefreshTokenService;
import com.careerpilot.auth.dto.LoginRequest;
import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.auth.dto.UserResponse;
import com.careerpilot.auth.service.AuthService;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService){
        this.authService=authService;
        this.refreshTokenService=refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse reponse = authService.register(request);
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(reponse);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request){

        LoginResponse response = refreshTokenService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        refreshTokenService.revokeRefreshToken(request.refreshToken());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {

        // Get authenticated user's ID
        UUID userId = UUID.fromString(authentication.getName());

        // Revoke all active refresh tokens
        refreshTokenService.revokeAllRefreshTokens(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication){
        return "Authenticated User id: "+authentication.getName();
    }
    
}
