# Task Plan: Issue #34 — Create Base Dockerfile Template

## What
Create a single reusable multi-stage Dockerfile at `infrastructure/docker/Dockerfile` that any service module can build.
Dependency #23 (parent POM setup) is closed.

## Services / Layers Involved
- **Infrastructure only** — no application layer changes.
- File created: `infrastructure/docker/Dockerfile`

## Design

### Stage 1 — Build (`eclipse-temurin:21-jdk`)
- Copy all POM files first (one `COPY` per module) before copying source — ensures the `mvn dependency:go-offline` layer is cached when only source changes.
- Accept `SERVICE` as a build arg (e.g. `auth`, `chat`).
- Run `mvn clean package -pl packages/${SERVICE} -am -DskipTests -B`.

### Stage 2 — Runtime (`eclipse-temurin:21-jre-alpine`)
- Create a non-root system user (`appuser` / `appgroup`) via `addgroup` + `adduser`.
- Copy `packages/${SERVICE}/target/${SERVICE}.jar` from the build stage (leverages the existing `<finalName>${project.artifactId}</finalName>` convention).
- `chown` the JAR, switch to `appuser`.
- `EXPOSE 8080`, `ENTRYPOINT ["java", "-jar", "app.jar"]`.

### Key Decisions
- **Single `SERVICE` build arg** — one Dockerfile for all services; build with `docker build --build-arg SERVICE=auth .` from repo root.
- **JRE-only runtime** — `eclipse-temurin:21-jre-alpine` (~90 MB smaller than JDK image).
- **No entrypoint script, no health check** — health checks belong in k8s probes; no unnecessary wrapper.

### SOLID / KISS
- SRP: one Dockerfile, one purpose.
- OCP: override `EXPOSE`/`ENTRYPOINT` via docker-compose or k8s manifests without touching the template.
- KISS: simplest shape satisfying all acceptance criteria.

## Reusable Components
None — this is purely an infrastructure artifact.

## Testing / Verification
No unit or integration tests apply. Manual verification:
1. `docker build --build-arg SERVICE=auth -t chat-auth .` — must succeed.
2. `docker inspect chat-auth` — confirm non-root `User`, JRE-alpine base.
3. Image size sanity check (~200–250 MB, not 500+).
