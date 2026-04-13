package com.marzuk.authorizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marzuk.components.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static KeyPair keyPair;
    private static KeyPair differentKeyPair;

    private JwtUtil jwtUtil;
    private PrivateKey signingKey;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        differentKeyPair = generator.generateKeyPair();
    }

    @BeforeEach
    void setUp() {
        String encodedPublicKey =
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String encodedPrivateKey =
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setPublicKey(encodedPublicKey);
        jwtProperties.setPrivateKey(encodedPrivateKey);

        jwtUtil = new JwtUtil(jwtProperties);
        signingKey = keyPair.getPrivate();
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
        String token =
                Jwts.builder()
                        .subject(UUID.randomUUID().toString())
                        .expiration(new Date(System.currentTimeMillis() + 60_000))
                        .signWith(differentKeyPair.getPrivate())
                        .compact();

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getSigningKey_returnsPrivateKeyWhenConfigured() {
        assertThat(jwtUtil.getSigningKey()).isEqualTo(keyPair.getPrivate());
    }

    @Test
    void constructor_succeedsWithoutPrivateKey() {
        String encodedPublicKey =
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        JwtProperties verifyOnlyProperties = new JwtProperties();
        verifyOnlyProperties.setPublicKey(encodedPublicKey);

        JwtUtil verifyOnlyUtil = new JwtUtil(verifyOnlyProperties);

        UUID userId = UUID.randomUUID();
        String token = buildValidToken(userId, "USER", "user@example.com");
        assertThat(verifyOnlyUtil.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void getSigningKey_throwsWhenPrivateKeyNotConfigured() {
        String encodedPublicKey =
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        JwtProperties verifyOnlyProperties = new JwtProperties();
        verifyOnlyProperties.setPublicKey(encodedPublicKey);

        JwtUtil verifyOnlyUtil = new JwtUtil(verifyOnlyProperties);

        assertThatThrownBy(verifyOnlyUtil::getSigningKey).isInstanceOf(IllegalStateException.class);
    }
}
