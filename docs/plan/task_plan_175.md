# Task Plan — Issue #175: Drop config-server module from monorepo

## What

Remove the `packages/config-server/` module entirely. The config-server was scaffolded but never implemented (it contains only a `pom.xml` — no Java sources, no `Dockerfile`, no `application.yml`). Configuration is already managed via environment variables / `.env` locally and will use Kubernetes ConfigMaps + Secrets in production.

## Why

Replacing a centralised Spring Cloud Config Server with Kubernetes-native config management (ConfigMaps + Secrets via Helm). This avoids an extra service to build, run, and maintain.

## Services / Layers Touched

| Area | Change |
|---|---|
| `packages/config-server/` | Delete entire directory |
| Root `pom.xml` | Remove `<module>packages/config-server</module>` |

No other module, Docker Compose service, Helm chart, CI workflow, or Java source file references `config-server`.

## Reusable Components

None — this is a pure deletion.

## SOLID / KISS Alignment

- **KISS**: Two deletions, zero new code. The module was never wired into any running service so no migration is needed.
- **SRP**: Removing an unused module keeps the monorepo honest — every listed module should be something that builds and ships.

## Tests

No tests required. The module has no source code, so there is nothing to test or regress. Build verification:

```bash
mvn clean package -DskipTests
```

## Steps

1. Delete `packages/config-server/` directory.
2. Remove `<module>packages/config-server</module>` from root `pom.xml`.
3. Run `mvn clean package -DskipTests` to confirm the remaining modules still compile.
