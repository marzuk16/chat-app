#!/bin/bash
set -euo pipefail

# Edit these ports to match docker-compose.yml bindings.
AUTH_PORT=8081
USER_PORT=8082
CHAT_PORT=8083
NOTIFICATION_PORT=8084
STORAGE_PORT=8085
API_GATEWAY_PORT=8086
ADMIN_PORTAL_PORT=8087

GREEN='\033[0;32m'
RED='\033[0;31m'
RESET='\033[0m'

check_service() {
    local name=$1
    local port=$2
    if curl -sf "http://localhost:${port}/actuator/health/liveness" > /dev/null 2>&1; then
        echo -e "${GREEN}[ UP ]${RESET} ${name} (port ${port})"
        return 0
    else
        echo -e "${RED}[DOWN]${RESET} ${name} (port ${port})"
        return 1
    fi
}

up=0

check_service "auth"         "$AUTH_PORT"         && ((up++)) || true
check_service "user"         "$USER_PORT"         && ((up++)) || true
check_service "chat"         "$CHAT_PORT"         && ((up++)) || true
check_service "notification" "$NOTIFICATION_PORT" && ((up++)) || true
check_service "storage"      "$STORAGE_PORT"      && ((up++)) || true
check_service "api-gateway"  "$API_GATEWAY_PORT"  && ((up++)) || true
check_service "admin-portal" "$ADMIN_PORTAL_PORT" && ((up++)) || true

echo ""
echo "${up}/7 services UP"
