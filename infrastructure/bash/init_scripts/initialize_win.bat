@echo off
setlocal

:: Log
echo Project initialisation has started ...

:: Generate JWT secret key using PowerShell
echo Generating JWT secret key ...
for /f %%i in ('powershell -NoProfile -Command "[System.BitConverter]::ToString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)).Replace(\"-\",\"\").ToLower()"') do set JWT_SECRET_KEY=%%i

:: Write .env file
echo .env file generation has started ...

(
    echo JWT_SECRET_KEY=%JWT_SECRET_KEY%
) > .env

echo .env file generation done.

:: Log
echo Project initialisation has completed. You are ready to run the services.

endlocal
