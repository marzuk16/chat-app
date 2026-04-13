#!/bin/bash
set -euo pipefail

echo "Building all JARs ..."
mvn clean package -DskipTests

TAG=$(git describe --tags --abbrev=0)
TAG=${TAG#v}
export TAG

echo "Releasing images with tag: $TAG"
docker compose build --no-cache
docker compose push

echo "Done. Images pushed with tag: $TAG"
