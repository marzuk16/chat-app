# Task Plan — Issue #134: Setup admin-portal module

## What
Bootstrap the `admin-portal` Maven module under `packages/admin-portal/` as a stateless
Thymeleaf MVC web UI. No JPA, no PostgreSQL, no direct database connection. All data access
goes through API calls to downstream services (user-service, etc.). The service must start
cleanly and expose a healthy `/actuator/health` endpoint.

## Services / Modules touched
- `packages/admin-portal/` — primary target
- Root `pom.xml` — already declares `<module>packages/admin-portal</module>`; no change needed

## Layers involved
Bootstrap only — no business logic. Layers wired:
- **Thymeleaf + Spring Web MVC** → via `web-starter`
- **Spring Security** → protect admin routes; `/actuator/health` stays public
- **Actuator** → via `web-starter`

No JPA, no datasource, no Kafka, no Testcontainers.

## Steps

### 1. Update `packages/admin-portal/pom.xml`
- Keep existing `web-starter` (pom type) — already bundles Thymeleaf, Validation, Actuator,
  `components` JAR
- Add:
  - `spring-boot-starter-security`
  - `maven-surefire-plugin` with `api.version=1.41` (Docker Engine 29.x compat)

### 2. Create main application class
`src/main/java/com/marzuk/adminportal/AdminPortalApp.java`
- `@SpringBootApplication(scanBasePackages = {"com.marzuk.adminportal", "com.marzuk.components"}, exclude = {UserDetailsServiceAutoConfiguration.class})`
- Lombok `@Slf4j` startup banner

### 3. Create `application.yml`
`src/main/resources/application.yml`
- No datasource block
- JWT: `JWT_PUBLIC_KEY` / `JWT_PRIVATE_KEY`
- Server port: `8080`
- Public paths: `/actuator/health`

### 4. Create integration test
`src/test/java/com/marzuk/adminportal/AdminPortalAppIntegrationTest.java`
- Plain `@SpringBootTest` — no Testcontainers needed (no DB)
- `@DynamicPropertySource`: generate RSA key pair for JWT
- Tests: context loads + `/actuator/health` returns `UP`

## Reusable components
Everything reusable already lives in `lib/components`:
- `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil`
- `BaseEntity`, `EventEnvelope<T>`, exception hierarchy, `GlobalExceptionHandler`

No new additions to `lib/` are needed for this issue.

## SOLID alignment
- **SRP**: portal has no data ownership; all persistence delegated to backend services via API
- **DIP**: security injected from `lib/components`; API clients (RestTemplate/WebClient) will
  be wired in later issues

## KISS check
- No JPA, PostgreSQL, Kafka, or Testcontainers — none of these belong in a pure MVC frontend
- No API client bean yet — that comes when actual admin screens are built (#136–#137)

## Test plan
| Test | Type | Verifies |
|---|---|---|
| Context loads | `@SpringBootTest` (no containers) | All auto-config wires correctly |
| `/actuator/health` returns UP | `@SpringBootTest` | Actuator exposed and healthy |
