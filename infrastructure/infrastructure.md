# Infrastructure

This folder contains all infrastructure-level configuration and scripts required to run the chat-app stack locally and in production. It has no Maven module — nothing here is compiled by the build.

## Contents

| Folder / File | Purpose |
|---|---|
| `docker-compose.yml` | Base Docker Compose — spins up PostgreSQL, Redis, Kafka, Zookeeper, MinIO |
| `docker-compose.override.yml` | Local dev overrides (port mappings, volume mounts) |
| `scripts/` | Shell scripts for environment setup, health checks, seed data |
| `dockerfiles/` | Base Dockerfile templates shared across services |

## Usage

Start the full local infrastructure stack:

```bash
docker compose up -d
```

Stop and remove containers:

```bash
docker compose down
```

Tear down including volumes (wipes all data):

```bash
docker compose down -v
```

## Related Issues

- [#32 Setup Docker Compose base infrastructure](https://github.com/marzuk16/chat-app/issues/32)
- [#33 Create infrastructure scripts](https://github.com/marzuk16/chat-app/issues/33)
- [#34 Create base Dockerfile template](https://github.com/marzuk16/chat-app/issues/34)
