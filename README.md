# chat-app
Chatting app using Spring Boot

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

## Local Development Setup

### 1. Clone the repository

```bash
git clone git@github.com:marzuk16/chat-app.git
cd chat-app
```

### 2. Run the initialisation script

Generates an RSA 4096 key pair for JWT signing and creates your `.env` file with all required environment variables.

**Linux / macOS:**
```bash
./infrastructure/bash/init_scripts/initialize.sh
```

**Windows:**
```bat
infrastructure\bash\init_scripts\initialize_win.bat
```

### 3. Build all JARs

```bash
mvn clean package -DskipTests
```

### 4. Start the full stack

```bash
docker compose up -d
```

This starts all infrastructure services (PostgreSQL, Redis, Kafka, MinIO, Mailpit) and all seven application services.

To start infrastructure only (useful when running services from your IDE):

```bash
docker compose up -d postgres redis kafka minio mailpit
```

### 5. Verify everything is running

```bash
./infrastructure/bash/monitor.sh
```

Polls the health endpoint of each service and reports their status.

---

## Services

| Service | URL | Notes |
|---|---|---|
| api-gateway | http://localhost:8086 | Main entry point |
| auth | http://localhost:8081 | |
| user | http://localhost:8082 | |
| chat | http://localhost:8083 | |
| notification | http://localhost:8084 | |
| storage | http://localhost:8085 | |
| admin-portal | http://localhost:8087 | |
| Mailpit UI | http://localhost:8025 | Inspect outgoing emails |
| MinIO Console | http://localhost:9001 | Object storage UI (`minioadmin` / `minioadmin`) |

Each service also exposes Swagger UI at `/swagger-ui.html`.

---

## Remote Debugging

All application services expose a JDWP debug socket. Connect your IDE debugger to `localhost` on the port for the service you want to inspect:

| Service | Debug port |
|---|---|
| auth | 8091 |
| user | 8092 |
| chat | 8093 |
| notification | 8094 |
| storage | 8095 |
| api-gateway | 8096 |
| admin-portal | 8097 |

---

## Common Commands

```bash
# View logs for a specific service
docker compose logs -f auth

# Restart a single service
docker compose restart auth

# Rebuild and restart a single service after a code change
mvn clean package -DskipTests -pl packages/auth
docker compose up -d --build auth

# Stop all containers
docker compose down

# Stop all containers and wipe all data (volumes)
docker compose down -v
```

---

## Build Commands

```bash
# Build entire monorepo
mvn clean package

# Build a single module
mvn clean package -pl packages/auth

# Build a module and its dependencies
mvn clean package -pl packages/auth -am

# Run tests for a single module
mvn test -pl packages/auth

# Skip tests during build
mvn clean install -DskipTests
```
