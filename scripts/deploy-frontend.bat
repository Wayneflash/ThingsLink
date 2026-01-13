@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Deploy Frontend Dist to Server
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

REM Read configuration (simplified version)
set REMOTE_HOST=
set REMOTE_PORT=22
set REMOTE_USER=root
set REMOTE_FRONTEND_PATH=/opt/iot-platform/frontend
set LOCAL_FRONTEND_DIST=frontend\dist

for /f "eol=# tokens=1,* delims==" %%a in (scripts\deploy-config.txt) do (
    if "%%a"=="REMOTE_HOST" set REMOTE_HOST=%%b
    if "%%a"=="REMOTE_PORT" set REMOTE_PORT=%%b
    if "%%a"=="REMOTE_USER" set REMOTE_USER=%%b
    if "%%a"=="REMOTE_FRONTEND_PATH" set REMOTE_FRONTEND_PATH=%%b
    if "%%a"=="LOCAL_FRONTEND_DIST" set LOCAL_FRONTEND_DIST=%%b
)

REM Check required configuration
if "!REMOTE_HOST!"=="" (
    echo [ERROR] REMOTE_HOST not configured
    pause
    exit /b 1
)

echo Configuration:
echo   Server: !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_PORT!
echo   Remote Path: !REMOTE_FRONTEND_PATH!
echo   Local Dist: !LOCAL_FRONTEND_DIST!
echo.

REM Check local dist directory
echo [1/5] Checking local file...
if not exist "!LOCAL_FRONTEND_DIST!" (
    echo [ERROR] dist directory does not exist: !LOCAL_FRONTEND_DIST!
    echo Please run scripts\build-frontend.bat to compile first
    pause
    exit /b 1
)
if not exist "!LOCAL_FRONTEND_DIST!\index.html" (
    echo [ERROR] index.html not found in dist directory
    echo Please run scripts\build-frontend.bat to compile first
    pause
    exit /b 1
)
echo [OK] dist directory exists
echo.

REM Check SSH tools
echo [2/5] Checking SSH tools...
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

REM Create archive
echo [3/5] Packaging frontend files...
cd frontend
if exist "..\frontend-dist.tar.gz" del /f /q "..\frontend-dist.tar.gz" >nul 2>&1
tar -czf ..\frontend-dist.tar.gz dist\*
if errorlevel 1 (
    echo [ERROR] Packaging failed
    cd ..
    pause
    exit /b 1
)
cd ..
echo [OK] Packaging complete
echo.

REM Upload archive
echo [4/5] Uploading frontend files (password required)...
scp -P !REMOTE_PORT! frontend-dist.tar.gz !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_FRONTEND_PATH!/frontend-dist.tar.gz
if errorlevel 1 (
    echo [ERROR] Upload failed
    del frontend-dist.tar.gz 2>nul
    pause
    exit /b 1
)
echo [OK] Upload successful
echo.

REM Remote extract
echo [5/5] Extracting frontend files (password required)...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "cd !REMOTE_FRONTEND_PATH! && rm -rf dist && tar -xzf frontend-dist.tar.gz && rm -f frontend-dist.tar.gz"
if errorlevel 1 (
    echo [WARNING] Extraction may have failed, please check manually
) else (
    echo [OK] Extraction successful
)
del frontend-dist.tar.gz 2>nul
echo.

echo ========================================
echo   Deployment Complete!
echo ========================================
echo.
echo Frontend Path: !REMOTE_FRONTEND_PATH!/dist
echo.
pause
endlocal
