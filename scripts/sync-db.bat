@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Database Migration Script
echo ========================================
echo.

REM Configuration
set REMOTE_HOST=117.72.222.8
set REMOTE_SSH_USER=root
set REMOTE_SSH_PORT=22
set REMOTE_DOCKER_CONTAINER=iot-mysql
set REMOTE_USER=root
set REMOTE_PASSWORD=root123456
set REMOTE_DATABASE=iot_platform

set LOCAL_DOCKER_CONTAINER=iot-mysql
set LOCAL_USER=root
set LOCAL_PASSWORD=root123456
set LOCAL_DATABASE=iot_platform

echo [INFO] Configuration:
echo   Local:  %LOCAL_DOCKER_CONTAINER%
echo   Remote: SSH %REMOTE_SSH_USER%@%REMOTE_HOST%:%REMOTE_SSH_PORT%
echo.

REM Check local Docker container
echo [1/3] Checking local Docker container...
docker ps | findstr /i "%LOCAL_DOCKER_CONTAINER%" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Local MySQL container is not running
    echo Please start: docker-compose up -d mysql
    pause
    exit /b 1
)
echo [OK] Local Docker container running
echo.

REM Check migration scripts
echo [2/3] Checking migration scripts...
if not exist "sql\migrations" (
    echo [ERROR] Migration directory not found
    pause
    exit /b 1
)

set MIGRATION_COUNT=0
for %%f in (sql\migrations\*.sql) do set /a MIGRATION_COUNT+=1

if %MIGRATION_COUNT%==0 (
    echo [WARNING] No migration scripts found
    pause
    exit /b 0
)
echo [OK] Found %MIGRATION_COUNT% migration script(s)
echo.

REM Execute local migrations
echo [3/3] Executing migrations...
echo.
echo === Local Database ===
set LOCAL_SUCCESS=0
for %%f in (sql\migrations\*.sql) do (
    echo [INFO] Executing: %%~nxf
    cmd /c "docker exec -i %LOCAL_DOCKER_CONTAINER% mysql -u%LOCAL_USER% -p%LOCAL_PASSWORD% %LOCAL_DATABASE% < %%f" 2>nul
    if errorlevel 1 (
        echo [WARNING] %%~nxf may have failed or already exists
    ) else (
        echo [OK] %%~nxf executed
        set /a LOCAL_SUCCESS+=1
    )
)
echo.
echo [OK] Local migrations completed: %LOCAL_SUCCESS%/%MIGRATION_COUNT%
echo.

REM Ask if sync to remote
echo.
set /p SYNC_REMOTE="Sync to remote server? (Y/N): "
if /i not "%SYNC_REMOTE%"=="Y" (
    echo [INFO] Skipping remote sync
    goto :END
)

echo.
echo === Remote Database ===
echo [INFO] SSH Password: Qiang531..
echo.

set REMOTE_SUCCESS=0
for %%f in (sql\migrations\*.sql) do (
    echo [INFO] Syncing: %%~nxf
    cmd /c "type %%f | ssh -p %REMOTE_SSH_PORT% %REMOTE_SSH_USER%@%REMOTE_HOST% ""docker exec -i %REMOTE_DOCKER_CONTAINER% mysql -u%REMOTE_USER% -p%REMOTE_PASSWORD% %REMOTE_DATABASE%""" 2>nul
    if errorlevel 1 (
        echo [WARNING] %%~nxf sync may have failed
    ) else (
        echo [OK] %%~nxf synced
        set /a REMOTE_SUCCESS+=1
    )
)
echo.
echo [OK] Remote sync completed: %REMOTE_SUCCESS%/%MIGRATION_COUNT%
echo.

:END
echo ========================================
echo   Migration Complete
echo ========================================
echo.
pause
endlocal
