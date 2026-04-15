package com.marzuk.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class PepperingPasswordEncoder implements PasswordEncoder {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final PasswordEncoder delegate;
    private final String pepper;

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(pepperedHash(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(pepperedHash(rawPassword), encodedPassword);
    }

    private String pepperedHash(CharSequence rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(
                    (rawPassword.toString() + pepper).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " not available", e);
        }
    }
}
