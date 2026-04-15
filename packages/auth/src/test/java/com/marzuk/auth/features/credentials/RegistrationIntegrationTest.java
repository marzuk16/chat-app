package com.marzuk.auth.features.credentials;

import static org.assertj.core.api.Assertions.assertThat;

import com.marzuk.components.constants.AppHeaders;
import com.marzuk.components.pojos.dto.auth.RegisterRequest;
import com.marzuk.components.pojos.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegistrationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("app.authorizer.mode", () -> "service");
        registry.add("app.security.pepper", () -> "test-pepper-value");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<RegisterRequest> requestWithActor(RegisterRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AppHeaders.USER_ID, UUID.randomUUID().toString());
        return new HttpEntity<>(body, headers);
    }

    private RegisterRequest validRequest(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("Secret123456!!");
        request.setUsername("alice");
        return request;
    }

    @Test
    void register_returns201AndUserId_forValidRequest() {
        ResponseEntity<Response> response = restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(validRequest("new@example.com")), Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(((Map<?, ?>) response.getBody().getData()).get("userId")).isNotNull();
    }

    @Test
    void register_returns409_forDuplicateEmail() {
        restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(validRequest("dup@example.com")), Response.class);

        ResponseEntity<Response> response = restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(validRequest("dup@example.com")), Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void register_returns400_forInvalidEmail() {
        RegisterRequest request = validRequest("not-an-email");

        ResponseEntity<Response> response = restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(request), Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void register_returns400_forWeakPassword() {
        RegisterRequest request = validRequest("weak@example.com");
        request.setPassword("short");

        ResponseEntity<Response> response = restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(request), Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void register_returns400_forBlankFields() {
        RegisterRequest request = new RegisterRequest();

        ResponseEntity<Response> response = restTemplate.postForEntity(
                url("/auth/register"), requestWithActor(request), Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }
}
