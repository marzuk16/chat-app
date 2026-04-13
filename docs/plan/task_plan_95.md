# Task Plan — Issue #95: Setup notification-service module

## What
Bootstrap the `notification-service` Maven module under `packages/notification/` with Spring Web,
Spring Data JPA, PostgreSQL, Redis, Kafka, Spring Security (JWT), Spring Mail, and the shared
`lib/components` library. The service must start cleanly, connect to `notification_db` and Redis,
and expose a healthy `/actuator/health` endpoint.

## Services / Modules touched
- `packages/notification/` — primary target
- Root `pom.xml` — already declares `<module>packages/notification</module>`; no change needed

## Layers involved
Bootstrap only — no business logic. Layers wired:
- **Spring Boot auto-configuration** → datasource, Redis, Kafka, security, mail
- **Shared security** → `SecurityConfig` + `JwtAuthenticationFilter` from `lib/components` (auto-scanned)
- **Health check** → Spring Actuator (bundled via `rest-starter`)

No Controller, Service, or Repository classes are created in this issue. Those come in #96–#98.

## Steps

### 1. Update `packages/notification/pom.xml`
- Keep existing `rest-starter` (pom type) — already bundles JPA, Validation, Actuator, SpringDoc,
  MapStruct, PostgreSQL driver, `components` JAR
- Add:
  - `spring-boot-starter-security`
  - `spring-boot-starter-data-redis`
  - `spring-kafka`
  - `spring-boot-starter-mail`
  - `spring-boot-starter-test` (test scope)
  - `testcontainers:junit-jupiter` + `testcontainers:postgresql` (test scope)
- Add `<build>` block: `<finalName>${project.artifactId}</finalName>` + `maven-surefire-plugin`

### 2. Create main application class
`src/main/java/com/marzuk/notification/NotificationApp.java`
- `@SpringBootApplication(scanBasePackages = {"com.marzuk.notification", "com.marzuk.components"}, exclude = {UserDetailsServiceAutoConfiguration.class})`
- Lombok `@Slf4j` startup banner

### 3. Create `application.yml`
`src/main/resources/application.yml`
- Datasource: `NOTIFICATION_DB_URL`, `NOTIFICATION_DB_USERNAME`, `NOTIFICATION_DB_PASSWORD`
  (defaults: `localhost:5432/notification_db`)
- Redis: `REDIS_HOST` / `REDIS_PORT`
- Kafka: `KAFKA_BOOTSTRAP_SERVERS`
- Mail: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- JWT: `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY`
- Server port: `8080`
- Public paths: `/actuator/health`
- `jpa.open-in-view: false`, `ddl-auto: validate`

### 4. Create integration test
`src/test/java/com/marzuk/notification/NotificationAppIntegrationTest.java`
- Testcontainers PostgreSQL (`notification_db`)
- `@DynamicPropertySource`: wire datasource + generate RSA key pair for JWT
- Exclude Redis, Kafka, and Mail auto-configuration (not needed for bootstrap verification)
- Tests: context loads + `/actuator/health` returns `UP`

## Reusable components
Everything reusable already lives in `lib/components`:
- `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil`
- `BaseEntity`, `EventEnvelope<T>`, exception hierarchy, `GlobalExceptionHandler`

No new additions to `lib/` are needed for this issue.

## SOLID alignment
- **SRP**: notification module has a single responsibility; no business logic at bootstrap
- **OCP**: `EventEnvelope<T>` pattern (for future Kafka listeners) is open for extension
- **DIP**: security, JWT, and exceptions injected from `lib/components` via component scanning

## Test plan
| Test | Type | Verifies |
|---|---|---|
| Context loads | Integration (Testcontainers PG) | All auto-config wires correctly |
| `/actuator/health` returns UP | Integration (Testcontainers PG) | Datasource connects; actuator exposed |
