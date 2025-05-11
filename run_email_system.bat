@echo off
setlocal enabledelayedexpansion

REM === Configuration ===
set "PROJECT_DIR=C:\Users\Raphael\Documents\Network Programming\EmailSystem"
set "DEFAULT_PASSWORD=supersecure"
set "SERVER_MAIN_CLASS=server.EmailServer"
set "CLIENT_MAIN_CLASS=client.core.EmailClientCLI"

REM === Check if Maven is installed ===
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed or not in PATH.
    pause
    exit /b 1
)

REM === Use existing env var or default ===
if not defined KEYSTORE_PASSWORD (
    set "KEYSTORE_PASSWORD=%DEFAULT_PASSWORD%"
    echo [INFO] No KEYSTORE_PASSWORD set. Using default for development: %KEYSTORE_PASSWORD%
) else (
    echo [INFO] Using system-defined KEYSTORE_PASSWORD.
)

REM === Export environment variable (session only) ===
set KEYSTORE_PASSWORD=%KEYSTORE_PASSWORD%

REM === Navigate to the project directory ===
cd /d "%PROJECT_DIR%"

REM === Start the server in a new terminal ===
echo [INFO] Launching Email Server...
start "EmailServer" cmd /k "set KEYSTORE_PASSWORD=%KEYSTORE_PASSWORD% && mvn exec:java -Dexec.mainClass=%SERVER_MAIN_CLASS%"

REM === Wait 3 seconds for server to boot ===
timeout /t 3 >nul

REM === Start 3 client terminals ===
echo [INFO] Launching 3 Email Clients...
start "EmailClient1" cmd /k "mvn exec:java -Dexec.mainClass=%CLIENT_MAIN_CLASS%"
start "EmailClient2" cmd /k "mvn exec:java -Dexec.mainClass=%CLIENT_MAIN_CLASS%"
start "EmailClient3" cmd /k "mvn exec:java -Dexec.mainClass=%CLIENT_MAIN_CLASS%"

REM === Finish ===
echo [INFO] All instances started. Press any key to exit this launcher window.
pause
endlocal
