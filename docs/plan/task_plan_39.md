# Task Plan — Issue #39: Setup auth-service module

## What

Wire the existing `packages/auth` module so it can start successfully and connect to its dedicated `auth_db` PostgreSQL database. This is pure infrastructure configuration — no business logic, no entities, no controllers. Those belong to the blocking issues (#40, #41).

## Note on config-server criterion

The original acceptance criterion "Fetches config from config-server" is **obsolete** — issue #175 removed config-server in favour of k8s-native config (ConfigMaps/Secrets + env vars). The auth service already reads JWT keys from environment variables. No config-server dependency will be added.

## Services / Layers Touched

- `lib/rest-starter` — add PostgreSQL driver and Testcontainers PostgreSQL to the shared dependency bundle
- `packages/auth` — update `application.yml` with datasource properties; no Java source changes
- `.env.example` — document new env vars

## Changes

### 1. `lib/rest-starter/pom.xml`

Add PostgreSQL driver (runtime scope) and Testcontainers PostgreSQL (test scope). Every REST microservice will connect to its own Postgres database, so the driver belongs in the shared starter rather than duplicated per-service.

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
```

### 2. `packages/auth/src/main/resources/application.yml`

Add datasource and JPA configuration driven by environment variables:

```yaml
spring:
  datasource:
    url: ${AUTH_DB_URL:jdbc:postgresql://localhost:5432/auth_db}
    username: ${AUTH_DB_USERNAME:postgres}
    password: ${AUTH_DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
    open-in-view: false
```

### 3. `.env.example`

```
AUTH_DB_URL=jdbc:postgresql://localhost:5432/auth_db
AUTH_DB_USERNAME=postgres
AUTH_DB_PASSWORD=postgres
```

## Reusable Components

None — PostgreSQL driver is infrastructure-level and goes into `rest-starter` which is already the shared bundle.

## Tests

- **Unit tests**: none required — this is configuration only, no logic to unit test.
- **Integration test**: one `@SpringBootTest` using Testcontainers PostgreSQL to verify:
  - Application context loads without errors
  - Datasource connects to `auth_db`
  - `/actuator/health` returns `UP`

## SOLID / KISS

- **SRP**: each change has a single concern — `rest-starter` owns "what deps all REST services share", `application.yml` owns "how auth connects to its DB".
- **KISS**: minimum viable wiring only. No Kafka, no entities, no security config beyond what already exists.

## Acceptance Criteria Mapping

| Criterion | How satisfied |
|---|---|
| Service starts on configured port | Port 8080 already in `application.yml`; datasource config unblocks startup |
| Connects to `auth_db` | Datasource URL, username, password added via env vars |
| Fetches config from config-server | Obsolete (config-server removed in #175) — env-var config already in place |
| `/actuator/health` returns UP | Actuator already provided by `rest-starter`; `/actuator/health` already in public paths |
