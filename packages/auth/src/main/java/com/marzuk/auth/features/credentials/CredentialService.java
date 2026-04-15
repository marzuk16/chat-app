package com.marzuk.auth.features.credentials;

import com.marzuk.auth.entities.Credential;
import com.marzuk.components.exception.DuplicateResourceException;
import com.marzuk.components.pojos.dto.auth.RegisterRequest;
import com.marzuk.components.pojos.dto.auth.RegisterResponse;
import com.marzuk.components.pojos.enums.auth.Role;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request, UUID actorId) {
        if (credentialRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email is already registered");
        }

        Credential credential = Credential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .locked(false)
                .emailVerified(false)
                .failedAttempts(0)
                .build();
        credential.setCreatedBy(actorId);
        credential.setUpdatedBy(actorId);

        Credential saved = credentialRepository.save(credential);

        return RegisterResponse.builder()
                .userId(saved.getId())
                .build();
    }
}
