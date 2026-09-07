package com.careerpilot.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.access-token-expiration}") long accessTokenExpiration){
                            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                            this.accessTokenExpiration = accessTokenExpiration;
                        }

    public String generateAccessToken(String subject){
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
}
