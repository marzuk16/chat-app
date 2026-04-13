package com.marzuk.authorizer;

import com.marzuk.components.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public JwtUtil(JwtProperties jwtProperties) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(JwtConstants.KEY_ALGORITHM);
            byte[] publicKeyBytes = Base64.getDecoder().decode(jwtProperties.getPublicKey());
            this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            if (jwtProperties.getPrivateKey() != null && !jwtProperties.getPrivateKey().isBlank()) {
                byte[] privateKeyBytes = Base64.getDecoder().decode(jwtProperties.getPrivateKey());
                this.privateKey =
                        keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            } else {
                this.privateKey = null;
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Failed to load RSA keys for JWT", exception);
        }
    }

    public PrivateKey getSigningKey() {
        if (privateKey == null) {
            throw new IllegalStateException(
                    "JWT private key is not configured — this service cannot sign tokens");
        }
        return privateKey;
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            return exception.getClaims();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid or malformed JWT token");
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public Claims validateToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims.getExpiration().before(new Date())) {
            throw new UnauthorizedException("JWT token has expired");
        }
        return claims;
    }
}
