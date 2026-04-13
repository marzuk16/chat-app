# Implementation Plan — Issue #54: Setup `user-service` module

## What

Bootstrap `packages/user` as a runnable Spring Boot service. The module directory and `pom.xml`
already exist but are empty — no `src/`, no main class, no configuration. This issue wires all
declared dependencies and verifies startup against `user_db`.

## Services / Layers Involved

- **Service**: `packages/user`
- **Layers**: infrastructure wiring only (no Controller / Service / Repository yet — those come in #55 and #60)

## Current State

| Already in place                         | Missing                    |
|------------------------------------------|----------------------------|
| `packages/user/pom.xml` (rest-starter only) | `src/` directory entirely |
| Module registered in root `pom.xml`      | `UserApp.java` main class  |
|                                          | `application.yml`          |
|                                          | `banner.txt`               |
|                                          | Redis, Kafka deps          |
|                                          | Integration test           |

## What `rest-starter` Already Brings (no need to repeat)

`spring-boot-starter-web` (transitive via springdoc), `spring-boot-starter-data-jpa`,
`spring-boot-starter-actuator`, `spring-boot-starter-validation`, `postgresql` (runtime),
`testcontainers:postgresql` (test), `components` (lib).

## Files to Create / Modify

| Action | Path |
|--------|------|
| Modify | `packages/user/pom.xml` |
| Create | `packages/user/src/main/java/com/marzuk/user/UserApp.java` |
| Create | `packages/user/src/main/resources/application.yml` |
| Create | `packages/user/src/main/resources/banner.txt` |
| Create | `packages/user/src/test/java/com/marzuk/user/UserAppIntegrationTest.java` |

## Step-by-Step Plan

### Step 1 — Update `packages/user/pom.xml`

Add Redis and Kafka dependencies on top of the existing `rest-starter` import:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

Add Testcontainers version property and Surefire plugin with `api.version=1.41` workaround
(same fix as auth — Docker Engine 29.x API negotiation):

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

### Step 2 — Create `UserApp.java`

```java
package com.marzuk.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.marzuk.user", "com.marzuk.components"})
public class UserApp {

    public static void main(String[] args) {
        SpringApplication.run(UserApp.class, args);
        log.info("###################################");
        log.info("User service started successfully!!!");
        log.info("###################################");
    }
}
```

### Step 3 — Create `application.yml`

```yaml
spring:
  application:
    name: user
  datasource:
    url: ${USER_DB_URL:jdbc:postgresql://localhost:5432/user_db}
    username: ${USER_DB_USERNAME:postgres}
    password: ${USER_DB_PASSWORD:postgres}
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
```

Notes:
- Port is hardcoded to `8080`; Docker Compose will control port mapping externally.
- Configuration is injected via environment variables (K8s ConfigMaps/Secrets, local `.env`).
  No Spring Cloud Config Server.

### Step 4 — Create `banner.txt`

ASCII art for "User" with port and profile info block, matching auth's style.

### Step 5 — Create `UserAppIntegrationTest.java`

```java
package com.marzuk.user;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAppIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db")
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
        registry.add("spring.autoconfigure.exclude", () ->
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.kafka.autoconfigure.KafkaAutoConfiguration");
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
future issues (#55, #60).

## Reusable Components

None — nothing here belongs in `lib/components`.

## SOLID & KISS Notes

- **SRP**: `UserApp` is a pure bootstrap entry point with no business logic.
- **KISS**: No feature code at this stage. Redis/Kafka are declared as dependencies and
  configured in `application.yml` but excluded in tests, keeping the integration test
  focused on the datasource connection AC.

## Tests

| Test | Type | Infra |
|------|------|-------|
| `applicationContext_loadsSuccessfully` | Integration | Testcontainers Postgres |
| `actuatorHealth_returnsUp_confirmingDatasourceConnected` | Integration | Testcontainers Postgres |
