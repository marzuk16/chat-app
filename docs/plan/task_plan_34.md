# Task Plan: Issue #34 — Create Base Dockerfile Template

## What
Create a single reusable Dockerfile at `infrastructure/docker/Dockerfile` that any service module can build.
The JAR is produced by `mvn clean package` on the host/CI before the Docker build — Docker's only job is packaging the runtime image.
Dependency #23 (parent POM setup) is closed.

## Services / Layers Involved
- **Infrastructure only** — no application layer changes.
- File created: `infrastructure/docker/Dockerfile`

## Design

### Single-stage — Runtime only (`eclipse-temurin:21-jre-alpine`)
- Accept `JAR_FILE` as a build arg — the pre-built JAR path relative to the repo root (e.g. `packages/auth/target/auth.jar`).
- Create a non-root system user (`appuser` / `appgroup`) via `addgroup` + `adduser`.
- Copy `${JAR_FILE}` into the image and `chown` it to `appuser`.
- Switch to `appuser`.
- `ENTRYPOINT ["java", "-jar", "app.jar"]` — no `EXPOSE`; ports are declared in docker-compose.

### Typical build invocation (from repo root)
```bash
mvn clean package -pl packages/auth -am -DskipTests
docker build \
  --build-arg JAR_FILE=packages/auth/target/auth.jar \
  -f infrastructure/docker/Dockerfile \
  -t chat-auth .
```

### Key Decisions
- **Pre-built JAR** — Maven runs on the host/CI where `.m2` cache is available; Docker only handles runtime packaging. Simpler, faster, better layer caching.
- **Single `JAR_FILE` build arg** — one Dockerfile for all services; caller supplies the JAR path.
- **JRE-only runtime** — `eclipse-temurin:21-jre-alpine` (~90 MB smaller than JDK image).
- **No `EXPOSE`, no health check** — ports are owned by docker-compose/k8s manifests; health checks belong in k8s probes.

### SOLID / KISS
- SRP: one Dockerfile, one purpose.
- OCP: override `EXPOSE`/`ENTRYPOINT` via docker-compose or k8s manifests without touching the template.
- KISS: simplest shape satisfying all acceptance criteria.

## Reusable Components
None — this is purely an infrastructure artifact.

## Testing / Verification
No unit or integration tests apply. Manual verification:
1. `mvn clean package -pl packages/auth -am -DskipTests`
2. `docker build --build-arg JAR_FILE=packages/auth/target/auth.jar -f infrastructure/docker/Dockerfile -t chat-auth .` — must succeed.
3. `docker inspect chat-auth` — confirm non-root `User`, JRE-alpine base, no exposed ports.
4. Image size sanity check (~100–150 MB, not 500+).
