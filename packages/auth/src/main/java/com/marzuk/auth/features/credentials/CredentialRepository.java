package com.marzuk.auth.features.credentials;

import com.marzuk.auth.entities.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByEmail(String email);
}
