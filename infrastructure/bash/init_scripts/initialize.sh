#!/bin/sh
set -e

### Log
echo "Project initialisation has started ..."

### Generate JWT secret key
echo "Generating JWT secret key ..."
JWT_SECRET_KEY=$(openssl rand -hex 32)

### Write .env file
echo ".env file generation has started ..."

cat > .env << ENDOFFILE
JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENDOFFILE

echo ".env file generation done."

### Log
echo "Project initialisation has completed. You are ready to run the services."
