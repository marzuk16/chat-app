# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire monorepo
mvn clean package

# Build a single module
mvn clean package -pl packages/auth

# Build a module and its dependencies
mvn clean package -pl packages/auth -am

# Run a specific service
mvn spring-boot:run -pl packages/auth

# Run tests for a single module
mvn test -pl packages/auth

# Skip tests during build
mvn clean install -DskipTests
```

Always use `mvn` directly, not `./mvnw`.

## Architecture

This is a Maven multi-module monorepo. The root `pom.xml` is a pure aggregator (packaging: `pom`) — it does **not** serve as a parent POM. Each module independently inherits from `spring-boot-starter-parent 3.3.3`.

```
chat-app/
├── lib/          # Shared libraries (resolved before packages/)
│   ├── components/    # BaseEntity, DTOs, JWT utils, Kafka envelopes, exceptions
│   ├── rest-starter/  # Dependency bundle for REST microservices (packaging: pom)
│   └── web-starter/   # Dependency bundle for SSR web modules (packaging: pom)
└── packages/     # Runnable services
    ├── config-server/  # Spring Cloud Config Server
    ├── auth/           # JWT auth — registration, login, token refresh
    ├── user/           # Profiles, contacts, presence
    ├── chat/           # Conversations, messages, WebSocket
    ├── notification/   # Push, email, device tokens
    ├── storage/        # MinIO-backed file storage
    ├── api-gateway/    # Routing, JWT validation, rate limiting, CORS
    └── admin-portal/   # SSR admin UI (Thymeleaf)
```

## Dependency Strategy

- `lib/rest-starter` and `lib/web-starter` are **type `pom` dependencies** — they act as dependency bills of materials (BOMs) imported via `<type>pom</type>`. Services do not compile against them directly.
- `lib/components` is a regular `jar` dependency providing shared domain types. Its Spring Boot plugin is configured with `skip=true` so it produces a plain JAR, not an executable fat-JAR.
- Most backend services depend on `rest-starter`. `admin-portal` uses `web-starter` instead (adds Thymeleaf). `api-gateway` currently has no starter dependency.

## Key Libraries

| Library | Version | Role |
|---|---|---|
| Spring Boot | 3.3.3 | Core framework |
| Lombok | 1.18.36 | Boilerplate reduction (annotation processor) |
| MapStruct | 1.6.3 | DTO mapping (annotation processor) |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI on all REST services |
| Testcontainers | 1.20.6 | Integration testing |

Planned infrastructure (not yet wired): PostgreSQL, Redis, Kafka/Zookeeper, MinIO.

## JAR Naming

All services set `<finalName>${project.artifactId}</finalName>`, so builds produce `auth.jar`, `chat.jar`, etc. — not `auth-0.0.1.jar`.

## Configuration

- Per-environment overrides go in `application-local.yml` (gitignored).
- Secrets are managed via environment variables; see `.env.example`.
- `config-server` will serve centralised configuration to all other services.
