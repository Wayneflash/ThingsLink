@echo off
chcp 65001 >nul

echo ========================================
echo   Check Development Server Databases
echo ========================================
echo.
echo Server: 117.72.162.225
echo.
echo This script will list all databases on the development server.
echo.

REM Check if we can connect to the server
echo [1/3] Testing SSH connection...
ssh root@117.72.162.225 "echo 'SSH connection successful'" 2>nul
if errorlevel 1 (
    echo [ERROR] Cannot connect to development server
    echo Possible reasons:
    echo   1. Server is down
    echo   2. SSH key/password issue
    echo   3. Network connectivity problem
    pause
    exit /b 1
)
echo [OK] SSH connection successful

echo.
echo [2/3] Checking MySQL container...
ssh root@117.72.162.225 "docker ps --format '{{.Names}}' | findstr /i mysql" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL container not found
    echo Please ensure MySQL is running in Docker on the development server
    pause
    exit /b 1
)
echo [OK] MySQL container found

echo.
echo [3/3] Listing databases...
echo.
echo Databases on development server (117.72.162.225):
echo ================================================
ssh root@117.72.162.225 "docker exec -i iot-mysql mysql -uroot -proot123456 -e 'SHOW DATABASES;'"
if errorlevel 1 (
    echo.
    echo [ERROR] Failed to list databases
    echo Possible reasons:
    echo   1. Wrong MySQL container name (expected: iot-mysql)
    echo   2. Wrong MySQL password
    echo   3. MySQL service not running properly
)

echo.
echo ========================================
echo   Database Check Complete
echo ========================================
echo.
echo Next steps:
echo   1. If 'iot_platform' database doesn't exist, run: scripts\init-remote-db.bat
echo   2. If you see other projects, be careful not to overwrite them
echo.
pause