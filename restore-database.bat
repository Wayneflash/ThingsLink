@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
echo ========================================
echo   Database Restore Script
echo ========================================
echo.

REM 自动检测脚本所在目录（不受路径影响）
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
if errorlevel 1 (
    echo [ERROR] Cannot change to script directory: %SCRIPT_DIR%
    echo Press any key to exit...
    pause
    exit /b 1
)

REM 检查Docker是否运行
docker ps --filter "name=iot-mysql" --format "{{.Names}}" 2>nul | findstr /i "iot-mysql" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL container (iot-mysql) is not running
    echo Please start Docker containers first: docker-compose up -d
    echo.
    echo Press any key to exit...
    pause
    exit /b 1
)

echo [INFO] MySQL container is running
echo.

REM 检查备份目录
set "BACKUP_DIR=%SCRIPT_DIR%backups"
if not exist "%BACKUP_DIR%" (
    echo [ERROR] Backup directory not found: %BACKUP_DIR%
    echo Please backup database first using backup-database.bat
    echo.
    echo Press any key to exit...
    pause
    exit /b 1
)

REM 查找备份文件
set "LATEST_FILE=%BACKUP_DIR%\iot_platform_latest.sql"

REM 如果命令行参数指定了文件，使用指定的文件
if not "%1"=="" (
    set "RESTORE_FILE=%1"
    if not exist "!RESTORE_FILE!" (
        echo [ERROR] Backup file not found: !RESTORE_FILE!
        echo Press any key to exit...
        pause
        exit /b 1
    )
) else (
    REM 使用latest文件
    if not exist "%LATEST_FILE%" (
        echo [ERROR] Latest backup file not found: %LATEST_FILE%
        echo.
        echo Available backup files:
        dir /b "%BACKUP_DIR%\iot_platform_*.sql" 2>nul
        echo.
        echo Usage: restore-database.bat [backup_file_path]
        echo Example: restore-database.bat backups\iot_platform_20260108_120000.sql
        echo.
        echo Press any key to exit...
        pause
        exit /b 1
    )
    set "RESTORE_FILE=%LATEST_FILE%"
)

echo [INFO] Restore file: %RESTORE_FILE%
echo.

REM 确认操作
echo [WARNING] This will restore the database from backup file.
echo [WARNING] All current data will be replaced!
echo.
set /p CONFIRM="Are you sure? (yes/no): "
if /i not "!CONFIRM!"=="yes" (
    echo Restore cancelled.
    echo Press any key to exit...
    pause
    exit /b 0
)

echo.
echo [INFO] Waiting for MySQL to be ready...
timeout /t 2 /nobreak >nul

REM 等待MySQL就绪
:wait_mysql
docker exec iot-mysql mysqladmin ping -uroot -proot123456 --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :wait_mysql
)

echo [OK] MySQL is ready
echo.

REM 删除并重新创建数据库（确保干净恢复）
echo [INFO] Dropping existing database...
docker exec iot-mysql mysql -uroot -proot123456 -e "DROP DATABASE IF EXISTS iot_platform;" 2>nul

echo [INFO] Creating database...
docker exec iot-mysql mysql -uroot -proot123456 -e "CREATE DATABASE iot_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul

echo [INFO] Restoring database from backup...
docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < "%RESTORE_FILE%" 2>&1

if errorlevel 1 (
    echo [ERROR] Database restore failed!
    echo Please check the error messages above.
    echo.
    echo Press any key to exit...
    pause
    exit /b 1
)

echo.
echo [OK] Database restored successfully!
echo.
echo [INFO] Restore completed at: %date% %time%
echo.
echo Press Enter to exit...
set /p DUMMY= >nul
