@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Database Sync Script (SSH + Docker)
echo ========================================
echo.

REM 设置配置（直接在脚本中设置，避免读取文件的复杂性）
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
echo   Remote: SSH %REMOTE_SSH_USER%@%REMOTE_HOST%:%REMOTE_SSH_PORT%
echo   Docker: %REMOTE_DOCKER_CONTAINER%
echo   Local:  %LOCAL_DOCKER_CONTAINER%
echo.

REM 检查本地Docker容器
echo [1/4] Checking local Docker container...
docker ps --filter "name=%LOCAL_DOCKER_CONTAINER%" --format "{{.Names}}" | findstr /i "%LOCAL_DOCKER_CONTAINER%" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Local MySQL container (%LOCAL_DOCKER_CONTAINER%) is not running
    echo Please start Docker containers first: docker-compose up -d
    pause
    exit /b 1
)
echo [OK] Local Docker container running
echo.

REM 检查迁移脚本目录
echo [2/4] Checking migration scripts...
if not exist "sql\migrations" (
    echo [ERROR] Migration directory not found: sql\migrations
    pause
    exit /b 1
)

set MIGRATION_COUNT=0
for %%f in (sql\migrations\*.sql) do (
    set /a MIGRATION_COUNT+=1
)

if %MIGRATION_COUNT%==0 (
    echo [WARNING] No migration scripts found in sql\migrations\
    pause
    exit /b 0
) else (
    echo [OK] Found %MIGRATION_COUNT% migration script(s)
)
echo.

REM 执行本地迁移
echo [3/4] Executing local migrations...
REM Create migration history table if not exists
if exist "sql\migrations\001_create_migration_history.sql" (
    docker exec -i %LOCAL_DOCKER_CONTAINER% mysql -u%LOCAL_USER% -p%LOCAL_PASSWORD% %LOCAL_DATABASE% < sql\migrations\001_create_migration_history.sql 2>nul
    if errorlevel 1 (
        echo [WARNING] Migration history table creation failed (may already exist)
    ) else (
        echo [INFO] Migration history table ensured
    )
)

set LOCAL_EXECUTED_COUNT=0
for %%f in (sql\migrations\*.sql) do (
    set MIGRATION_FILE=%%~nxf
    set MIGRATION_PATH=%%f
    
    REM Check if already executed locally
    docker exec %LOCAL_DOCKER_CONTAINER% mysql -u%LOCAL_USER% -p%LOCAL_PASSWORD% %LOCAL_DATABASE% -e "SELECT COUNT(*) FROM tb_migration_history WHERE migration_file='%MIGRATION_FILE%';" 2>nul | findstr "0" >nul 2>&1
    
    if not errorlevel 1 (
        echo [INFO] Executing: %MIGRATION_FILE%
        docker exec -i %LOCAL_DOCKER_CONTAINER% mysql -u%LOCAL_USER% -p%LOCAL_PASSWORD% %LOCAL_DATABASE% < "%MIGRATION_PATH%" 2>nul
        if errorlevel 1 (
            echo [ERROR] Failed to execute: %MIGRATION_FILE%
        ) else (
            echo [OK] %MIGRATION_FILE% executed
            docker exec %LOCAL_DOCKER_CONTAINER% mysql -u%LOCAL_USER% -p%LOCAL_PASSWORD% %LOCAL_DATABASE% -e "INSERT INTO tb_migration_history (migration_file, status) VALUES ('%MIGRATION_FILE%', 'success') ON DUPLICATE KEY UPDATE status='success';" 2>nul
            set /a LOCAL_EXECUTED_COUNT+=1
        )
    ) else (
        echo [SKIP] %MIGRATION_FILE% already executed locally
    )
)
echo [OK] Local migrations completed. Executed: %LOCAL_EXECUTED_COUNT% script(s)
echo.

REM Sync to remote database
echo [4/4] Syncing to remote database...
echo.
echo [INFO] Will connect via SSH. Password: Qiang531..
echo.

REM Create migration history table on remote if not exists
type "sql\migrations\001_create_migration_history.sql" | ssh -p %REMOTE_SSH_PORT% %REMOTE_SSH_USER%@%REMOTE_HOST% "docker exec -i %REMOTE_DOCKER_CONTAINER% mysql -u%REMOTE_USER% -p%REMOTE_PASSWORD% %REMOTE_DATABASE%" 2>nul
if errorlevel 1 (
    echo [WARNING] Remote migration history table creation failed (may already exist or SSH issue)
) else (
    echo [INFO] Remote migration history table ensured
)

set REMOTE_EXECUTED_COUNT=0
for %%f in (sql\migrations\*.sql) do (
    set MIGRATION_FILE=%%~nxf
    set MIGRATION_PATH=%%f
    
    REM Check if already executed remotely
    ssh -p %REMOTE_SSH_PORT% %REMOTE_SSH_USER%@%REMOTE_HOST% "docker exec %REMOTE_DOCKER_CONTAINER% mysql -u%REMOTE_USER% -p%REMOTE_PASSWORD% %REMOTE_DATABASE% -e \"SELECT COUNT(*) FROM tb_migration_history WHERE migration_file='%MIGRATION_FILE%';\" 2>/dev/null" | findstr "0" >nul 2>&1
    
    if not errorlevel 1 (
        echo [INFO] Syncing: %MIGRATION_FILE%
        type "%MIGRATION_PATH%" | ssh -p %REMOTE_SSH_PORT% %REMOTE_SSH_USER%@%REMOTE_HOST% "docker exec -i %REMOTE_DOCKER_CONTAINER% mysql -u%REMOTE_USER% -p%REMOTE_PASSWORD% %REMOTE_DATABASE%" 2>nul
        if errorlevel 1 (
            echo [WARNING] %MIGRATION_FILE% sync failed
        ) else (
            echo [OK] %MIGRATION_FILE% synced
            ssh -p %REMOTE_SSH_PORT% %REMOTE_SSH_USER%@%REMOTE_HOST% "docker exec %REMOTE_DOCKER_CONTAINER% mysql -u%REMOTE_USER% -p%REMOTE_PASSWORD% %REMOTE_DATABASE% -e \"INSERT INTO tb_migration_history (migration_file, status) VALUES ('%MIGRATION_FILE%', 'success') ON DUPLICATE KEY UPDATE status='success';\"" 2>nul
            set /a REMOTE_EXECUTED_COUNT+=1
        )
    ) else (
        echo [SKIP] %MIGRATION_FILE% already synced remotely
    )
)
echo [OK] Remote migrations completed. Synced: %REMOTE_EXECUTED_COUNT% script(s)
echo.

echo ========================================
echo   Sync Complete
echo ========================================
echo.
pause
endlocal
