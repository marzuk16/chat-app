# Infrastructure

This folder contains all infrastructure-level configuration and scripts required to run the chat-app stack locally and in production. It has no Maven module — nothing here is compiled by the build.

## Folder Structure

```
infrastructure/
├── bash/
│   ├── init_scripts/
│   │   ├── initialize.sh        # Generate RSA keys, create .env, install git hooks
│   │   └── initialize_win.bat   # Windows equivalent
│   ├── push_images/
│   │   ├── push_release.sh      # Build JARs → Docker images → push to DockerHub (tagged)
│   │   └── push_snapshot.sh     # Same but for snapshot versions
│   ├── git_hooks/
│   │   └── pre-commit           # Formatting/linting check before commit
│   └── monitor.sh               # Poll /actuator/health on all local services
└── cluster-setup/
    ├── build-env/
    │   ├── dev.yaml             # Helm values for dev cluster
    │   ├── staging.yaml         # Helm values for staging cluster
    │   └── production.yaml      # Helm values for production cluster
    └── helm-charts/
        ├── chat-app-stack/      # Main application chart (all services)
        │   ├── Chart.yaml
        │   └── templates/
        │       ├── configs/
        │       │   ├── app-config.yaml       # Shared ConfigMap (timezone, limits, flags)
        │       │   └── service-urls.yaml     # Internal service URLs
        │       ├── deployments/
        │       │   ├── auth.yaml
        │       │   ├── user.yaml
        │       │   ├── chat.yaml
        │       │   ├── notification.yaml
        │       │   ├── storage.yaml
        │       │   ├── api-gateway.yaml
        │       │   └── admin-portal.yaml
        │       ├── secret.yaml               # JWT keys, DB creds, MinIO credentials
        │       ├── service.yaml
        │       └── ingress.yaml              # Route external traffic via api-gateway
        ├── ingress-deployment/               # nginx ingress controller values
        └── cert-manager/                     # cert-manager + Let's Encrypt ClusterIssuer
```

## Local Development

A single `docker-compose.yml` at the project root covers everything — infrastructure services and all Spring Boot application services.

> **MinIO note:** MinIO is included in docker-compose for local development only. In Kubernetes it runs on a dedicated VM outside the cluster and is accessed via the `MINIO_ENDPOINT` env var injected through the ConfigMap.

```bash
# Start everything
docker compose up -d

# Start only infrastructure (Postgres, Redis, Kafka, MinIO)
docker compose up -d postgres redis kafka minio

# Stop all containers
docker compose down

# Tear down including volumes (wipes all data)
docker compose down -v

# View logs for a specific service
docker compose logs -f auth
```

### First-time Setup

Run the initialisation script to generate RSA keys for JWT signing, create your `.env` file, and install git hooks:

```bash
./infrastructure/bash/init_scripts/initialize.sh
```

On Windows:
```bash
infrastructure\bash\init_scripts\initialize_win.bat
```

### Health Monitoring (Local)

Poll the health endpoint of all locally running services:

```bash
./infrastructure/bash/monitor.sh
```

## Docker Images

Each service has its own `Dockerfile` under `packages/{service}/Dockerfile`. Multi-stage build: Maven compiles the JAR in the builder stage, the runtime stage uses a slim JRE image.

Images are published to DockerHub under `marzuk16/chat-{service}:{version}`.

```bash
# Push a release (tagged with latest git tag)
./infrastructure/bash/push_images/push_release.sh

# Push a snapshot (tagged with git commit SHA)
./infrastructure/bash/push_images/push_snapshot.sh
```

## Kubernetes Deployment

Configuration is managed via Kubernetes ConfigMaps and Secrets, templated through Helm. No Spring Cloud Config Server.

| Config type | Mechanism |
|---|---|
| Non-sensitive (URLs, ports, feature flags) | Kubernetes ConfigMap → env vars |
| Sensitive (JWT keys, DB passwords, MinIO creds) | Kubernetes Secret → env vars |
| App defaults (app name, logging) | `application.yml` baked into the JAR |

### MinIO (dedicated VM)

MinIO is **not deployed to Kubernetes**. It runs on a dedicated VM in the cluster and is treated as external infrastructure — the same way a managed database would be.

| Env var | Description |
|---|---|
| `MINIO_ENDPOINT` | URL of the MinIO VM (e.g. `http://192.168.1.x:9000`) |
| `MINIO_ACCESS_KEY` | MinIO access key — injected from Kubernetes Secret |
| `MINIO_SECRET_KEY` | MinIO secret key — injected from Kubernetes Secret |

The `storage` service uses presigned URLs so clients download files directly from MinIO without proxying through the backend.

### Deploy to a cluster

```bash
# Deploy to staging
helm upgrade --install chat-app ./infrastructure/cluster-setup/helm-charts/chat-app-stack \
  -f ./infrastructure/cluster-setup/build-env/staging.yaml \
  --namespace chat-staging --create-namespace

# Deploy to production
helm upgrade --install chat-app ./infrastructure/cluster-setup/helm-charts/chat-app-stack \
  -f ./infrastructure/cluster-setup/build-env/production.yaml \
  --namespace chat-production --create-namespace
```

## CI/CD (GitHub Actions)

| Workflow | Trigger | Action |
|---|---|---|
| `build-test` | PR to `staging` | Run all tests |
| `build-snapshot-push` | Push to `test` branch | Build + push snapshot images to DockerHub |
| `build-release-push` | Push tag matching `v*` | Build + push release images to DockerHub |

## Related Issues

- [#32 Setup Docker Compose](https://github.com/marzuk16/chat-app/issues/32)
- [#33 Create infrastructure scripts](https://github.com/marzuk16/chat-app/issues/33)
- [#155 Create umbrella Helm chart](https://github.com/marzuk16/chat-app/issues/155)
- [#156 Create Helm values for production](https://github.com/marzuk16/chat-app/issues/156)
- [#157 Create Helm values for staging](https://github.com/marzuk16/chat-app/issues/157)
- [#158 Create deploy-staging.sh](https://github.com/marzuk16/chat-app/issues/158)
- [#159 Create deploy-production.sh](https://github.com/marzuk16/chat-app/issues/159)
