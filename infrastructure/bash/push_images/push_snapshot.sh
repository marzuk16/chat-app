#!/bin/bash
set -euo pipefail

echo "Building all JARs ..."
mvn clean package -DskipTests

TAG=$(git rev-parse --short HEAD)
export TAG

echo "Releasing snapshot images with tag: $TAG"
docker compose build --no-cache
docker compose push

echo "Done. Images pushed with tag: $TAG"
