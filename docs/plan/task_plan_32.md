# Implementation Plan — Issue #32: Setup single Docker Compose for all services and infrastructure

## What

Create a single `docker-compose.yml` at the project root that provisions all infrastructure and all seven application services for local development. Extend `initialize.sh` / `initialize_win.bat` and `.env.example` to cover every required environment variable.

## Files Changed

| File | Action |
|---|---|
| `docker-compose.yml` | Create |
| `infrastructure/docker/postgres-init/init.sql` | Create |
| `.env.example` | Update |
| `infrastructure/bash/init_scripts/initialize.sh` | Update |
| `infrastructure/bash/init_scripts/initialize_win.bat` | Update |
| `README.md` | Update |

## Infrastructure Services

| Service | Image | Volume |
|---|---|---|
| postgres | `postgres:17-alpine` | `postgres_data` |
| redis | `redis:7-alpine` | — |
| kafka | `apache/kafka:3.9` (KRaft, no ZooKeeper) | `kafka_data` |
| minio | `minio/minio:latest` | `minio_data` |
| mailpit | `axllent/mailpit:latest` | — |

## Application Services

All seven services use the shared `infrastructure/docker/Dockerfile` with a per-service `JAR_FILE` build arg. Port bindings are on `127.0.0.1` only.

| Service | App port | Debug port |
|---|---|---|
| auth | 8081 | 8091 |
| user | 8082 | 8092 |
| chat | 8083 | 8093 |
| notification | 8084 | 8094 |
| storage | 8085 | 8095 |
| api-gateway | 8086 | 8096 |
| admin-portal | 8087 | 8097 |

JDWP remote debugging enabled on all app services via `JAVA_TOOL_OPTIONS`.

## Network Topology

Four isolated bridge networks — each service only has access to what it needs:

| Network | Members |
|---|---|
| `servicenet` | all app services, minio, mailpit |
| `dbnet` | postgres, auth, user, chat, notification, storage |
| `redisnet` | redis, user, chat, notification, api-gateway |
| `kafkanet` | kafka, user, chat, notification |

## Health Checks & depends_on

All infrastructure services expose healthchecks. Application services declare `depends_on` with `condition: service_healthy` on their required infrastructure.

## Environment Variables

`initialize.sh` generates `.env` with JWT keys (RSA 4096) and sensible local defaults for all other variables. `.env.example` documents all required variables with placeholder values.

## Testing

Manual verification:
1. `./infrastructure/bash/init_scripts/initialize.sh` — `.env` generated correctly
2. `mvn clean package -DskipTests` — all JARs built
3. `docker compose up -d postgres redis kafka minio mailpit` — infra healthchecks pass
4. `docker compose up -d` — all app services start
5. `./infrastructure/bash/monitor.sh` — all services report healthy
6. `docker compose down -v` — clean teardown
