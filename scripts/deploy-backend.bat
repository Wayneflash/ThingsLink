@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Deploy Backend JAR to Server
echo ========================================
echo.
echo Project Path: %CD%
echo.

REM Check configuration file
if not exist "scripts\deploy-config.txt" (
    echo [ERROR] Configuration file scripts\deploy-config.txt does not exist
    echo Please create configuration file first
    pause
    exit /b 1
)

REM Read configuration (simplified version, directly set variables)
set REMOTE_HOST=
set REMOTE_PORT=22
set REMOTE_USER=root
set REMOTE_BACKEND_PATH=/opt/iot-platform/backend
set REMOTE_BACKEND_SERVICE=iot-platform
set LOCAL_BACKEND_JAR=backend\target\iot-platform.jar

for /f "eol=# tokens=1,* delims==" %%a in (scripts\deploy-config.txt) do (
    if "%%a"=="REMOTE_HOST" set REMOTE_HOST=%%b
    if "%%a"=="REMOTE_PORT" set REMOTE_PORT=%%b
    if "%%a"=="REMOTE_USER" set REMOTE_USER=%%b
    if "%%a"=="REMOTE_BACKEND_PATH" set REMOTE_BACKEND_PATH=%%b
    if "%%a"=="REMOTE_BACKEND_SERVICE" set REMOTE_BACKEND_SERVICE=%%b
    if "%%a"=="LOCAL_BACKEND_JAR" set LOCAL_BACKEND_JAR=%%b
)

REM Check required configuration
if "!REMOTE_HOST!"=="" (
    echo [ERROR] REMOTE_HOST not configured
    pause
    exit /b 1
)

echo Configuration:
echo   Server: !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_PORT!
echo   Remote Path: !REMOTE_BACKEND_PATH!
echo   Service Name: !REMOTE_BACKEND_SERVICE!
echo   Local JAR: !LOCAL_BACKEND_JAR!
echo.

REM Check local jar file
echo [1/4] Checking local file...
if not exist "!LOCAL_BACKEND_JAR!" (
    echo [ERROR] JAR file does not exist: !LOCAL_BACKEND_JAR!
    echo Please run scripts\build-backend.bat to compile first
    pause
    exit /b 1
)
echo [OK] JAR file exists
echo.

REM Check SSH tools
echo [2/4] Checking SSH tools...
where ssh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ssh command not found, please install OpenSSH
    pause
    exit /b 1
)
where scp >nul 2>&1
if errorlevel 1 (
    echo [ERROR] scp command not found, please install OpenSSH
    pause
    exit /b 1
)
echo [OK] SSH tools available
echo.

REM Upload jar file
echo [3/4] Uploading JAR package (password required)...
scp -P !REMOTE_PORT! "!LOCAL_BACKEND_JAR!" !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_BACKEND_PATH!/iot-platform.jar
if errorlevel 1 (
    echo [ERROR] Upload failed
    pause
    exit /b 1
)
echo [OK] Upload successful
echo.

REM Restart service
echo [4/4] Restarting backend service (password required)...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "systemctl restart !REMOTE_BACKEND_SERVICE!"
if errorlevel 1 (
    echo [WARNING] Service restart may have failed, please check manually
) else (
    echo [OK] Service restarted
)
echo.

echo ========================================
echo   Deployment Complete!
echo ========================================
echo.
pause
endlocal
