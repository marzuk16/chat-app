#!/bin/sh
set -e

### Log
echo "Project initialisation has started ..."

### Generate RSA 4096 key pair for JWT signing/verification
echo "Generating RSA 4096 key pair for JWT ..."

TEMP_DIR=$(mktemp -d)
openssl genrsa -out "$TEMP_DIR/private.pem" 4096 2>/dev/null
openssl rsa -in "$TEMP_DIR/private.pem" -pubout -out "$TEMP_DIR/public.pem" 2>/dev/null

# Convert to DER, then Base64-encode as single-line strings for .env storage
JWT_PRIVATE_KEY=$(openssl pkcs8 -topk8 -nocrypt -in "$TEMP_DIR/private.pem" -outform DER | base64 -w 0)
JWT_PUBLIC_KEY=$(openssl rsa -pubin -in "$TEMP_DIR/public.pem" -outform DER 2>/dev/null | base64 -w 0)

rm -rf "$TEMP_DIR"

### Generate password pepper (32 random bytes, base64-encoded)
echo "Generating password pepper ..."
AUTH_PASSWORD_PEPPER=$(openssl rand -base64 32)

### Write .env file
echo ".env file generation has started ..."

cat > .env << ENDOFFILE
# JWT (generated — do not edit manually)
JWT_PRIVATE_KEY=${JWT_PRIVATE_KEY}
JWT_PUBLIC_KEY=${JWT_PUBLIC_KEY}

# Auth (generated — do not edit manually)
AUTH_PASSWORD_PEPPER=${AUTH_PASSWORD_PEPPER}

# Spring
ACTIVE_PROFILES=compose,dev
JPA_DDL_AUTO=validate

# PostgreSQL
POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-secret}

# MinIO
MINIO_ROOT_USER=${MINIO_ROOT_USER:-minioadmin}
MINIO_ROOT_PASSWORD=${MINIO_ROOT_PASSWORD:-minioadmin}
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-minioadmin}

# Mail (Mailpit handles SMTP locally — no credentials needed)
MAIL_USERNAME=
MAIL_PASSWORD=
ENDOFFILE

echo ".env file generation done."

### Log
echo "Project initialisation has completed. You are ready to run the services."
