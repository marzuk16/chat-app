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

:: Write .env file
echo .env file generation has started ...

(
    echo # JWT (generated -- do not edit manually)
    echo JWT_PRIVATE_KEY=%JWT_PRIVATE_KEY%
    echo JWT_PUBLIC_KEY=%JWT_PUBLIC_KEY%
    echo.
    echo # Spring
    echo ACTIVE_PROFILES=compose,dev
    echo JPA_DDL_AUTO=update
    echo.
    echo # PostgreSQL
    echo POSTGRES_USER=postgres
    echo POSTGRES_PASSWORD=postgres
    echo.
    echo # MinIO
    echo MINIO_ROOT_USER=minioadmin
    echo MINIO_ROOT_PASSWORD=minioadmin
    echo MINIO_ACCESS_KEY=minioadmin
    echo MINIO_SECRET_KEY=minioadmin
    echo.
    echo # Mail (Mailpit handles SMTP locally -- no credentials needed)
    echo MAIL_USERNAME=
    echo MAIL_PASSWORD=
) > .env

echo .env file generation done.

:: Log
echo Project initialisation has completed. You are ready to run the services.

endlocal
