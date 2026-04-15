package com.marzuk.auth.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.marzuk.components.pojos.enums.auth.Role;
import org.junit.jupiter.api.Test;

class CredentialTest {

    @Test
    void builder_defaults_lockedIsFalse() {
        Credential credential = Credential.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();

        assertThat(credential.isLocked()).isFalse();
    }

    @Test
    void builder_defaults_failedAttemptsIsZero() {
        Credential credential = Credential.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();

        assertThat(credential.getFailedAttempts()).isZero();
    }

    @Test
    void builder_defaults_emailVerifiedIsFalse() {
        Credential credential = Credential.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();

        assertThat(credential.isEmailVerified()).isFalse();
    }

    @Test
    void builder_setsRole_user() {
        Credential credential = Credential.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();

        assertThat(credential.getRole()).isEqualTo(Role.USER);
    }
}
