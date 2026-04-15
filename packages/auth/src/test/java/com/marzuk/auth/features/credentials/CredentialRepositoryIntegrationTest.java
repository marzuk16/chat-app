package com.marzuk.auth.features.credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marzuk.auth.entities.Credential;
import com.marzuk.components.pojos.enums.auth.Role;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class CredentialRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired
    private CredentialRepository credentialRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        registry.add(
                "jwt.public-key",
                () -> Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        registry.add(
                "jwt.private-key",
                () -> Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
    }

    private Credential buildCredential(String email) {
        Credential credential = Credential.builder()
                .email(email)
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();
        credential.setCreatedBy("system");
        credential.setUpdatedBy("system");
        return credential;
    }

    @Test
    void findByEmail_returnsCredential_whenEmailExists() {
        credentialRepository.save(buildCredential("alice@example.com"));

        Optional<Credential> result = credentialRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailNotFound() {
        Optional<Credential> result = credentialRepository.findByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void save_throwsDataIntegrityViolationException_onDuplicateEmail() {
        credentialRepository.save(buildCredential("duplicate@example.com"));

        assertThatThrownBy(() -> credentialRepository.saveAndFlush(buildCredential("duplicate@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
