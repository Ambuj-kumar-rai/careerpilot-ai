
package com.careerpilot.auth.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerpilot.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);

    void deleteAllByExpiresAtBefore(OffsetDateTime dateTime);

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}
