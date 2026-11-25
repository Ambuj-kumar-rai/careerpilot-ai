
package com.careerpilot.auth.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.mapper.UserMapper;
import com.careerpilot.auth.security.JwtService;
import com.careerpilot.auth.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careerpilot.auth.entity.RefreshToken;
import com.careerpilot.auth.entity.User;
import com.careerpilot.auth.repository.RefreshTokenRepository;
import com.careerpilot.auth.service.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiration;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserMapper userMapper,
            JwtService jwtService,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }

    @Override
    @Transactional
    public String createRefreshToken(User user) {

        // Generate 32 cryptographically secure random bytes
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        // Convert random bytes into a URL-safe token
        String refreshTokenValue = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Hash the original token using SHA-256
        String tokenHash = hashToken(refreshTokenValue);

        // Set token timestamps
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusNanos(
                refreshTokenExpiration * 1_000_000L
        );

        // Create refresh token entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(expiresAt);

        // Persist token hash in database
        refreshTokenRepository.save(refreshToken);

        // Return original token, never its hash
        return refreshTokenValue;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
@Transactional
@Override
    public LoginResponse refreshAccessToken(String refreshTokenValue){
    // 1. Hash the incoming refresh token
        String tokenHash = hashToken(refreshTokenValue);

    // 2. Find its database record
        RefreshToken existingToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    // 3. Check whether the token was already revoked
        if(existingToken.getRevokedAt() != null){
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }

    // 4. Check expiration
        if(!existingToken.getExpiresAt().isAfter(now)){
            throw new InvalidCredentialsException("Refresh token has expired");
        }

    // 5. Generate a replacement refresh token
        User user = existingToken.getUser();

        String newRefreshTokenValue = createRefreshToken(user);

    // 6. Find the newly created database record
        String newTokenHash = hashToken(newRefreshTokenValue);

        RefreshToken newToken = refreshTokenRepository
                .findByTokenHash(newTokenHash)
                .orElseThrow(() -> new IllegalStateException("New refresh token was not saved"));

    // 7. Revoke the old refresh token
        existingToken.setRevokedAt(now);

    // 8. Link the old token to its replacement
        existingToken.setReplacedByTokenId(newToken.getId());

        refreshTokenRepository.save(existingToken);

    // 9. Generate a new access token
        String newAccessToken = jwtService.generateAccessToken(user.getId().toString());

    // 10. Return both new tokens
        return userMapper.toLoginResponse(
                user,
                newAccessToken,
                newRefreshTokenValue
        );

    }

    @Override
    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {

        // 1. Hash the incoming refresh token
        String tokenHash = hashToken(refreshTokenValue);

        // 2. Find the token in the database
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {

                    // 3. Revoke only if it is not already revoked
                    if (token.getRevokedAt() == null) {

                        token.setRevokedAt(
                                OffsetDateTime.now(ZoneOffset.UTC)
                        );

                        refreshTokenRepository.save(token);
                    }
                });
    }

    @Override
    @Transactional
    public void revokeAllRefreshTokens(UUID userId) {

        // 1. Find all non-revoked refresh tokens for this user
        List<RefreshToken> activeTokens =
                refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId);

        // 2. Get current UTC time
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 3. Revoke every active refresh token
        for (RefreshToken token : activeTokens) {
            token.setRevokedAt(now);
        }

        // 4. Save all updated tokens
        refreshTokenRepository.saveAll(activeTokens);
    }
}
