package com.marzuk.adminportal;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminPortalAppIntegrationTest {

    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
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

    @Test
    void applicationContext_loadsSuccessfully() {
        // context loads without errors — verified by the fact that the test reaches this point
    }

    @Test
    void actuatorHealth_returnsUp() {
        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/actuator/health", Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("status", "UP");
    }
}
