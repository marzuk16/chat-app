# Task Plan — Issue #112: Setup file-service module

## What
Bootstrap the `storage` (file-service) Maven module under `packages/storage/` with Spring Web,
Spring Data JPA, PostgreSQL, MinIO Java SDK, Spring Security (JWT), and the shared
`lib/components` library. The service must start cleanly, connect to `file_db`, and expose a
healthy `/actuator/health` endpoint.

## Services / Modules touched
- `packages/storage/` — primary target
- Root `pom.xml` — already declares `<module>packages/storage</module>`; no change needed

## Layers involved
Bootstrap only — no business logic. Layers wired:
- **Spring Boot auto-configuration** → datasource, security
- **MinIO client bean** → a `@Configuration` class producing a `MinioClient` bean from env vars
- **Shared security** → `SecurityConfig` + `JwtAuthenticationFilter` from `lib/components` (auto-scanned)
- **Health check** → Spring Actuator (bundled via `rest-starter`)

No Controller, Service, or Repository classes are created in this issue. Those come in #113–#114.

## Steps

### 1. Update `packages/storage/pom.xml`
- Keep existing `rest-starter` (pom type) — already bundles JPA, Validation, Actuator, SpringDoc,
  MapStruct, PostgreSQL driver, `components` JAR
- Add:
  - `spring-boot-starter-security`
  - `io.minio:minio:8.5.17`
  - `spring-boot-starter-test` (test scope)
  - `testcontainers:junit-jupiter` + `testcontainers:postgresql` (test scope)
- Add `maven-surefire-plugin` with `api.version=1.41` (Docker Engine 29.x compat)

### 2. Create main application class
`src/main/java/com/marzuk/storage/StorageApp.java`
- `@SpringBootApplication(scanBasePackages = {"com.marzuk.storage", "com.marzuk.components"}, exclude = {UserDetailsServiceAutoConfiguration.class})`
- Lombok `@Slf4j` startup banner

### 3. Create MinIO configuration
`src/main/java/com/marzuk/storage/config/MinioConfig.java`
- `@Configuration` class producing a `MinioClient` `@Bean`
- Properties bound from `minio.*` namespace: `minio.endpoint`, `minio.access-key`, `minio.secret-key`

### 4. Create `application.yml`
`src/main/resources/application.yml`
- Datasource: `STORAGE_DB_URL`, `STORAGE_DB_USERNAME`, `STORAGE_DB_PASSWORD`
  (defaults: `localhost:5432/file_db`)
- MinIO: `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
- JWT: `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY`
- Server port: `8080`
- Public paths: `/actuator/health`
- `jpa.open-in-view: false`, `ddl-auto: validate`

### 5. Create integration test
`src/test/java/com/marzuk/storage/StorageAppIntegrationTest.java`
- Testcontainers PostgreSQL (`file_db`)
- `@DynamicPropertySource`: wire datasource, generate RSA key pair for JWT, stub MinIO props
- Exclude MinIO auto-configuration (no running MinIO needed for bootstrap verification)
- Tests: context loads + `/actuator/health` returns `UP`

## Reusable components
Everything reusable already lives in `lib/components`:
- `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil`
- `BaseEntity`, `EventEnvelope<T>`, exception hierarchy, `GlobalExceptionHandler`

No new additions to `lib/` are needed for this issue.

## SOLID alignment
- **SRP**: storage module has a single responsibility; MinIO config is isolated in its own
  `@Configuration` class
- **DIP**: security, JWT, and exceptions injected from `lib/components` via component scanning;
  MinIO client abstracted behind a Spring bean

## KISS check
- No Kafka, Redis, or mail wiring — this service doesn't need them at bootstrap
- MinIO config is a single class producing one bean — minimal surface
- No bucket-creation logic or custom health indicator at this stage

## Test plan
| Test | Type | Verifies |
|---|---|---|
| Context loads | Integration (Testcontainers PG) | All auto-config wires correctly |
| `/actuator/health` returns UP | Integration (Testcontainers PG) | Datasource connects; actuator exposed |
