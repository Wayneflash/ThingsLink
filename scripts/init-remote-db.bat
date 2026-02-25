@echo off
chcp 65001 >nul

echo ========================================
echo   Initialize Remote Database
echo ========================================
echo.
echo Server: 117.72.162.225
echo.
echo WARNING: This server may already have other projects running!
echo Please verify this is the correct development server.
echo.
echo This script will:
echo   1. Check if MySQL container exists
echo   2. Create database 'iot_platform' if not exists
echo   3. Import initial schema from init.sql
echo   4. Apply migrations from sql/migrations/
echo.
echo IMPORTANT: This will NOT affect existing databases other than 'iot_platform'
echo.
set /p CONFIRM="Continue? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Operation cancelled.
    pause
    exit /b 0
)

echo.
echo [1/5] Checking MySQL container on development server...
REM Check if MySQL container exists
ssh root@117.72.162.225 "docker ps --format '{{.Names}}' | findstr /i mysql" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL container not found on development server
    echo Please ensure MySQL is running in Docker on 117.72.162.225
    pause
    exit /b 1
)
echo [OK] MySQL container found

echo.
echo [2/5] Checking existing databases...
echo Current databases on development server:
ssh root@117.72.162.225 "docker exec -i iot-mysql mysql -uroot -proot123456 -e 'SHOW DATABASES;'"
echo.
set /p CONFIRM="Do you see 'iot_platform' database? If yes, it will be overwritten. Continue? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Operation cancelled.
    pause
    exit /b 0
)

echo.
echo [3/5] Creating database 'iot_platform'...
REM Connect via SSH and create database
ssh root@117.72.162.225 "docker exec -i iot-mysql mysql -uroot -proot123456 -e 'CREATE DATABASE IF NOT EXISTS iot_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES LIKE \"iot_platform\";'"
if errorlevel 1 (
    echo [ERROR] Failed to create database
    echo Possible reasons:
    echo   1. Wrong MySQL container name (expected: iot-mysql)
    echo   2. Wrong MySQL password
    echo   3. MySQL service not running
    pause
    exit /b 1
)
echo [OK] Database created/verified

echo.
echo [4/5] Importing initial schema...
REM Import init.sql
type init.sql | ssh root@117.72.162.225 "docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform"
if errorlevel 1 (
    echo [ERROR] Failed to import initial schema
    pause
    exit /b 1
)
echo [OK] Initial schema imported

echo.
echo [5/5] Applying migrations...
REM Apply all migration files
set MIGRATION_COUNT=0
set SUCCESS_COUNT=0
for %%f in (sql\migrations\*.sql) do (
    set /a MIGRATION_COUNT+=1
    echo [INFO] Applying: %%~nxf
    type "%%f" | ssh root@117.72.162.225 "docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform" 2>nul
    if errorlevel 1 (
        echo [WARNING] %%~nxf may have failed
    ) else (
        echo [OK] %%~nxf applied
        set /a SUCCESS_COUNT+=1
    )
)

echo.
echo ========================================
echo   Remote Database Initialization Complete
echo ========================================
echo.
echo Summary:
echo   - Database: iot_platform
echo   - Migrations applied: %SUCCESS_COUNT%/%MIGRATION_COUNT%
echo   - Server: 117.72.162.225
echo.
echo Next steps:
echo   1. Run scripts\start-ssh-tunnel.bat
echo   2. Run scripts\start-dev.bat
echo.
pause