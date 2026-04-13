# Implementation Plan — Issue #71: Setup `chat-service` module

## What

Bootstrap `packages/chat` as a runnable Spring Boot service. The module directory and `pom.xml`
already exist but are empty — no `src/`, no main class, no configuration. This issue wires all
declared dependencies (including WebSocket) and verifies startup against `chat_db`.

## Services / Layers Involved

- **Service**: `packages/chat`
- **Layers**: infrastructure wiring only (no Controller / Service / Repository yet — those come in #72, #73, #74)

## Current State

| Already in place                         | Missing                      |
|------------------------------------------|------------------------------|
| `packages/chat/pom.xml` (rest-starter only) | `src/` directory entirely |
| Module registered in root `pom.xml`      | `ChatApp.java` main class    |
|                                          | `application.yml`            |
|                                          | `banner.txt`                 |
|                                          | WebSocket, Security, Redis, Kafka deps |
|                                          | Integration test             |

## What `rest-starter` Already Brings (no need to repeat)

`spring-boot-starter-web` (transitive via springdoc), `spring-boot-starter-data-jpa`,
`spring-boot-starter-actuator`, `spring-boot-starter-validation`, `postgresql` (runtime),
`testcontainers:postgresql` (test), `components` (lib), `mapstruct`.

## Files to Create / Modify

| Action | Path |
|--------|------|
| Modify | `packages/chat/pom.xml` |
| Create | `packages/chat/src/main/java/com/marzuk/chat/ChatApp.java` |
| Create | `packages/chat/src/main/resources/application.yml` |
| Create | `packages/chat/src/main/resources/banner.txt` |
| Create | `packages/chat/src/test/java/com/marzuk/chat/ChatAppIntegrationTest.java` |

## Step-by-Step Plan

### Step 1 — Update `packages/chat/pom.xml`

Add Testcontainers version property and Surefire plugin with `api.version=1.41` workaround
(same fix as auth and user — Docker Engine 29.x API negotiation):

```xml
<properties>
    <java.version>21</java.version>
    <testcontainers.version>1.21.0</testcontainers.version>
</properties>

<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <systemPropertyVariables>
                    <api.version>1.41</api.version>
                </systemPropertyVariables>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Dependencies to add:

```xml
<!-- WebSocket support — not provided by rest-starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<!-- Required because lib/components ships SecurityConfig picked up by component scan -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
```

### Step 2 — Create `ChatApp.java`

```java
package com.marzuk.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.marzuk.chat", "com.marzuk.components"},
        exclude = {UserDetailsServiceAutoConfiguration.class}
)
public class ChatApp {

    public static void main(String[] args) {
        SpringApplication.run(ChatApp.class, args);
        log.info("###################################");
        log.info("Chat service started successfully!!!");
        log.info("###################################");
    }
}
```

### Step 3 — Create `application.yml`

```yaml
spring:
  application:
    name: chat
  datasource:
    url: ${CHAT_DB_URL:jdbc:postgresql://localhost:5432/chat_db}
    username: ${CHAT_DB_USERNAME:postgres}
    password: ${CHAT_DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
    open-in-view: false
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

server:
  port: 8080

jwt:
  public-key: ${JWT_PUBLIC_KEY}
  private-key: ${JWT_PRIVATE_KEY}

app:
  security:
    public-paths:
      - /actuator/health
```

Notes:
- Port is hardcoded to `8080`; Docker Compose controls port mapping externally.
- Configuration is injected via environment variables (K8s ConfigMaps/Secrets, local `.env`).

### Step 4 — Create `banner.txt`

ASCII art for "Chat" with port and profile info block, matching auth/user style.

### Step 5 — Create `ChatAppIntegrationTest.java`

```java
package com.marzuk.chat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatAppIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("chat_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @LocalServerPort int port;

    @Autowired TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.autoconfigure.exclude", () ->
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        registry.add("jwt.public-key",
                () -> Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        registry.add("jwt.private-key",
                () -> Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
    }

    @Test
    void applicationContext_loadsSuccessfully() { }

    @Test
    void actuatorHealth_returnsUp_confirmingDatasourceConnected() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", Map.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("status", "UP");
    }
}
```

Redis and Kafka auto-configurations are excluded in tests — those integrations belong in
future issues (#72, #73, #74).

## Reusable Components

None — nothing here belongs in `lib/components`.

## SOLID & KISS Notes

- **SRP**: `ChatApp` is a pure bootstrap entry point with no business logic.
- **KISS**: No feature code at this stage. WebSocket, Redis, and Kafka are declared as
  dependencies and configured in `application.yml` but excluded in tests, keeping the
  integration test focused on the datasource connection AC.

## Tests

| Test | Type | Infra |
|------|------|-------|
| `applicationContext_loadsSuccessfully` | Integration | Testcontainers Postgres |
| `actuatorHealth_returnsUp_confirmingDatasourceConnected` | Integration | Testcontainers Postgres |
