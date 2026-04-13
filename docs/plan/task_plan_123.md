# Task Plan — Issue #123: Setup api-gateway module

## What

Complete the dependency scaffolding of `packages/api-gateway` so it starts, exposes `/actuator/health`, and has the right dependency set for its future work (routing, JWT validation, rate limiting, CORS).

The module skeleton already exists (GatewayApp.java, application.yml, pom.xml) but is missing the web, actuator, and Redis starters required to actually boot.

## Services / Layers Touched

| Area | Change |
|---|---|
| `packages/api-gateway/pom.xml` | Add `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-data-redis`, `lombok` |
| `packages/api-gateway/src/main/resources/application.yml` | Expose actuator health endpoint; add Redis connection placeholders with `spring.redis.connect-timeout` guard so the service starts without Redis |

No new Java classes — `SecurityConfig`, `JwtAuthenticationFilter`, and `JwtUtil` from `lib/components` are already picked up via component scanning in `GatewayApp`.

## Reusable Components

None required — all shared security infrastructure already lives in `lib/components`.

## Dependency Notes

- **#23** (parent POM) — CLOSED, already satisfied.
- **#36** (Config Client files) — CLOSED and superseded by #175. Config Server approach dropped; configuration comes from env vars / K8s ConfigMaps + Secrets. Config Client dependency is intentionally excluded.

## SOLID / KISS Alignment

- **KISS**: Pure scaffolding — add four dependencies and minimal YAML config. Zero custom classes.
- **SRP**: The gateway owns cross-cutting concerns (routing, auth, rate limiting, CORS). No business logic belongs here.
- **OCP**: Rate limiting and route configuration are extension points deferred to follow-up issues (#125, #126).

## Tests

No unit tests — there is no custom logic to assert against. The acceptance criterion (service starts, `/actuator/health` returns UP) is verified by a successful build:

```bash
mvn clean package -pl packages/api-gateway -am
```

A Testcontainers smoke test (`contextLoads`) will be added in #125 when Redis rate limiting is wired, at which point a running Redis is already required.

## Steps

1. Update `pom.xml` — add the four dependencies listed above.
2. Update `application.yml` — expose actuator health endpoint; add Redis host/port/password placeholders.
3. Run `mvn clean package -pl packages/api-gateway -am` to confirm the build.
4. Commit and push.
