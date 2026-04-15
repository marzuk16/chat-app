package com.marzuk.auth.features.credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marzuk.auth.entities.Credential;
import com.marzuk.components.exception.DuplicateResourceException;
import com.marzuk.components.pojos.dto.auth.RegisterRequest;
import com.marzuk.components.pojos.dto.auth.RegisterResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialService credentialService;

    private RegisterRequest buildRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Secret1234");
        request.setUsername("alice");
        return request;
    }

    @Test
    void register_returnsUserId_whenInputIsValid() {
        UUID expectedId = UUID.randomUUID();
        Credential saved = Credential.builder()
                .email("alice@example.com")
                .passwordHash("$2a$hashed")
                .build();
        ReflectionTestUtils.setField(saved, "id", expectedId);

        when(credentialRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret1234")).thenReturn("$2a$hashed");
        when(credentialRepository.save(any(Credential.class))).thenReturn(saved);

        RegisterResponse response = credentialService.register(buildRequest(), UUID.randomUUID());

        assertThat(response.getUserId()).isEqualTo(expectedId);
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(credentialRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new Credential()));

        assertThatThrownBy(() -> credentialService.register(buildRequest(), UUID.randomUUID()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");

        verify(credentialRepository, never()).save(any());
    }

    @Test
    void register_encodesPassword_andNeverStoresRawPassword() {
        Credential saved = Credential.builder()
                .email("alice@example.com")
                .passwordHash("$2a$hashed")
                .build();
        ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());

        when(credentialRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret1234")).thenReturn("$2a$hashed");
        when(credentialRepository.save(any(Credential.class))).thenReturn(saved);

        credentialService.register(buildRequest(), UUID.randomUUID());

        verify(passwordEncoder).encode("Secret1234");
        verify(credentialRepository).save(
                argThat(credential -> "$2a$hashed".equals(credential.getPasswordHash())
                        && !"Secret1234".equals(credential.getPasswordHash())));
    }
}
