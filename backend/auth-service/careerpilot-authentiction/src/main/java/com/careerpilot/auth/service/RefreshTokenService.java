
package com.careerpilot.auth.service;

import com.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.auth.entity.User;

import java.util.UUID;

public interface RefreshTokenService {

    String createRefreshToken(User user);

    LoginResponse refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    void revokeAllRefreshTokens(UUID userId);

}
