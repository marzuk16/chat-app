package com.chatapp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<String> extractUserId(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    public Optional<String> extractUsername(String token) {
        return parseClaims(token)
                .map(claims -> claims.get(SecurityConstants.CLAIM_USERNAME, String.class));
    }

    public Optional<String> extractRoles(String token) {
        return parseClaims(token)
                .map(claims -> claims.get(SecurityConstants.CLAIM_ROLES, String.class));
    }

    public boolean isValid(String token) {
        return parseClaims(token).isPresent();
    }
}
