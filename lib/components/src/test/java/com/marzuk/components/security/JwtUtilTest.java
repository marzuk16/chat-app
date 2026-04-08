package com.marzuk.components.security;

import com.marzuk.components.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET_KEY = "test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final String DIFFERENT_SECRET_KEY = "different-secret-key-that-is-long-enough-for-hmac";

    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtUtil = new JwtUtil(jwtProperties);
        signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private String buildValidToken(UUID userId, String role, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("email", email)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();
    }

    private String buildExpiredToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void extractUserId_returnsCorrectUuid() {
        UUID userId = UUID.randomUUID();
        String token = buildValidToken(userId, "USER", "user@example.com");

        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = buildValidToken(UUID.randomUUID(), "ADMIN", "admin@example.com");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = buildValidToken(UUID.randomUUID(), "USER", "user@example.com");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void isTokenExpired_returnsFalseForValidToken() {
        String token = buildValidToken(UUID.randomUUID(), "USER", "user@example.com");

        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_returnsTrueForExpiredToken() {
        String token = buildExpiredToken(UUID.randomUUID());

        assertThat(jwtUtil.isTokenExpired(token)).isTrue();
    }

    @Test
    void validateToken_returnsClaimsForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = buildValidToken(userId, "USER", "user@example.com");

        var claims = jwtUtil.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
    }

    @Test
    void validateToken_throwsForExpiredToken() {
        String token = buildExpiredToken(UUID.randomUUID());

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validateToken_throwsForMalformedToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("this.is.garbage"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validateToken_throwsForWrongSigningKey() {
        SecretKey differentKey = Keys.hmacShaKeyFor(DIFFERENT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(differentKey)
                .compact();

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(UnauthorizedException.class);
    }
}
