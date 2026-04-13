# Task Plan — Issue #33: Create Infrastructure Bash Scripts

## What

Create DevOps shell scripts under `infrastructure/bash/` to automate Docker image builds/pushes,
local service health monitoring, and code-format enforcement via a git pre-commit hook.
Also wire the Spotless Maven plugin (Google Java Format) into the root `pom.xml` so the hook
has something to invoke.

## Scope

**Infrastructure only** — no service code is touched.

| Deliverable | Path |
|---|---|
| Spotless plugin | `pom.xml` (root `<build>`) |
| Release image push | `infrastructure/bash/push_images/push_release.sh` |
| Snapshot image push | `infrastructure/bash/push_images/push_snapshot.sh` |
| Service health monitor | `infrastructure/bash/monitor.sh` |
| Pre-commit hook | `infrastructure/bash/git_hooks/pre-commit` |
| `.gitignore` update | `.gitignore` (add `.secrets/`) |

Scripts that already exist (`initialize.sh`, `initialize_win.bat`) are **out of scope**.

## Design

### `pom.xml` — Spotless Plugin

Add `com.diffplug.spotless:spotless-maven-plugin:2.43.0` to the root aggregator's
`<build><plugins>` block with `<inherited>false</inherited>`.

Configuration:
- **Google Java Format 1.22.0** (AOSP style, 4-space indent)
- Source globs cover `lib/**/src/{main,test}/java/**/*.java` and `packages/**/src/{main,test}/java/**/*.java`
- `<removeUnusedImports/>` enabled

Developers run:
- `mvn spotless:check` — fails if any file is mis-formatted
- `mvn spotless:apply` — auto-fixes formatting in place

### `git_hooks/pre-commit`

```sh
#!/bin/sh
set -e
echo "Running Spotless formatter check ..."
mvn spotless:check -q
echo "Formatter check passed."
```

Must be executable. Installed to `.git/hooks/` via `initialize.sh` (existing script, future work).

### `push_images/push_release.sh`

1. `mvn clean package -DskipTests` — build all JARs
2. `git describe --tags --abbrev=0` — get latest tag (e.g. `v1.2.0`)
3. Strip leading `v`, export as `TAG`
4. `docker compose build --no-cache`
5. `docker compose push`

Assumes `docker-compose.yml` at project root uses `${TAG}` in image names.

### `push_images/push_snapshot.sh`

Same flow but `TAG=$(git rev-parse --short HEAD)` instead of a git tag.

### `monitor.sh`

Configurable port map at the top (edit to match `docker-compose.yml` port bindings):

```
AUTH_PORT=8081, USER_PORT=8082, CHAT_PORT=8083,
NOTIFICATION_PORT=8084, STORAGE_PORT=8085,
API_GATEWAY_PORT=8086, ADMIN_PORTAL_PORT=8087
```

For each service:
- `curl -sf http://localhost:${PORT}/actuator/health/liveness`
- Print `[ UP ]` (green) or `[DOWN]` (red)
- Summary line: `X/7 services UP`

## Tests

These are shell scripts and Maven config — no unit tests apply.

Manual verification:
1. `mvn spotless:check` passes on clean checkout
2. Introduce a formatting violation → `mvn spotless:check` fails; `mvn spotless:apply` fixes it
3. `./infrastructure/bash/monitor.sh` — shows `[DOWN]` for all services when nothing is running
4. Pre-commit hook blocks a commit that contains mis-formatted Java
5. Push scripts are tested once `docker-compose.yml` is created (future issue)
