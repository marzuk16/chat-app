@echo off
setlocal enabledelayedexpansion

:: Log
echo Project initialisation has started ...

:: Generate RSA 4096 key pair using PowerShell
echo Generating RSA 4096 key pair for JWT ...

for /f "delims=" %%i in ('powershell -NoProfile -Command ^
    "$rsa = [System.Security.Cryptography.RSA]::Create(4096); ^
    [System.Convert]::ToBase64String($rsa.ExportPkcs8PrivateKey())"') do set JWT_PRIVATE_KEY=%%i

for /f "delims=" %%i in ('powershell -NoProfile -Command ^
    "$rsa = [System.Security.Cryptography.RSA]::Create(4096); ^
    $rsa.ImportPkcs8PrivateKey([System.Convert]::FromBase64String('%JWT_PRIVATE_KEY%'), [ref]$null); ^
    [System.Convert]::ToBase64String($rsa.ExportSubjectPublicKeyInfo())"') do set JWT_PUBLIC_KEY=%%i

:: Generate password pepper (32 random bytes, base64-encoded)
echo Generating password pepper ...

for /f "delims=" %%i in ('powershell -NoProfile -Command ^
    "$bytes = New-Object byte[] 32; ^
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes); ^
    [System.Convert]::ToBase64String($bytes)"') do set AUTH_PASSWORD_PEPPER=%%i

:: Apply defaults for sensitive values if not already set in the environment
if not defined POSTGRES_USER set POSTGRES_USER=postgres
if not defined POSTGRES_PASSWORD set POSTGRES_PASSWORD=postgres
if not defined MINIO_ROOT_USER set MINIO_ROOT_USER=minioadmin
if not defined MINIO_ROOT_PASSWORD set MINIO_ROOT_PASSWORD=minioadmin
if not defined MINIO_ACCESS_KEY set MINIO_ACCESS_KEY=minioadmin
if not defined MINIO_SECRET_KEY set MINIO_SECRET_KEY=minioadmin

:: Write .env file
echo .env file generation has started ...

(
    echo # Auth + JWT (generated -- do not edit manually)
    echo AUTH_PASSWORD_PEPPER=%AUTH_PASSWORD_PEPPER%
    echo JWT_PRIVATE_KEY=%JWT_PRIVATE_KEY%
    echo JWT_PUBLIC_KEY=%JWT_PUBLIC_KEY%
    echo.
    echo # Spring
    echo ACTIVE_PROFILES=compose,dev
    echo JPA_DDL_AUTO=validate
    echo.
    echo # PostgreSQL
    echo POSTGRES_USER=%POSTGRES_USER%
    echo POSTGRES_PASSWORD=%POSTGRES_PASSWORD%
    echo.
    echo # MinIO
    echo MINIO_ROOT_USER=%MINIO_ROOT_USER%
    echo MINIO_ROOT_PASSWORD=%MINIO_ROOT_PASSWORD%
    echo MINIO_ACCESS_KEY=%MINIO_ACCESS_KEY%
    echo MINIO_SECRET_KEY=%MINIO_SECRET_KEY%
    echo.
    echo # Mail (Mailpit handles SMTP locally -- no credentials needed)
    echo MAIL_USERNAME=
    echo MAIL_PASSWORD=
) > .env

echo .env file generation done.

:: Log
echo Project initialisation has completed. You are ready to run the services.

endlocal
