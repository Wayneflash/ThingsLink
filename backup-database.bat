@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
echo ========================================
echo   Database Backup Script
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

REM 创建备份目录
set "BACKUP_DIR=%SCRIPT_DIR%backups"
if not exist "%BACKUP_DIR%" (
    echo [INFO] Creating backup directory...
    mkdir "%BACKUP_DIR%" 2>nul
    if errorlevel 1 (
        echo [ERROR] Failed to create backup directory: %BACKUP_DIR%
        echo Press any key to exit...
        pause
        exit /b 1
    )
    echo [OK] Backup directory created: %BACKUP_DIR%
) else (
    echo [INFO] Backup directory exists: %BACKUP_DIR%
)

REM 生成备份文件名（使用日期时间）
set "BACKUP_FILE=%BACKUP_DIR%\iot_platform_backup.sql"
set "LATEST_FILE=%BACKUP_DIR%\iot_platform_latest.sql"

echo.
echo [INFO] Backing up database...
echo [INFO] Backup file: %BACKUP_FILE%
echo [INFO] Latest file: %LATEST_FILE%
echo.

REM 执行备份
docker exec iot-mysql mysqldump -uroot -proot123456 --single-transaction --routines --triggers iot_platform > "%BACKUP_FILE%" 2>&1

if errorlevel 1 (
    echo [ERROR] Database backup failed!
    echo Please check the error messages above.
    echo.
    echo Press any key to exit...
    pause
    exit /b 1
)

REM 检查备份文件是否生成且不为空
if not exist "%BACKUP_FILE%" (
    echo [ERROR] Backup file not created!
    echo Press any key to exit...
    pause
    exit /b 1
)

for %%A in ("%BACKUP_FILE%") do (
    set "FILE_SIZE=%%~zA"
    if !FILE_SIZE! LSS 1000 (
        echo [ERROR] Backup file is too small (!FILE_SIZE! bytes), backup may have failed
        echo Press any key to exit...
        pause
        exit /b 1
    )
)

REM 复制为latest文件
copy /Y "%BACKUP_FILE%" "%LATEST_FILE%" >nul 2>&1

echo [OK] Database backup completed successfully!
echo.
echo Backup files:
echo   - Backup: %BACKUP_FILE%
echo   - Latest: %LATEST_FILE%
echo.

REM 显示文件大小
for %%A in ("%BACKUP_FILE%") do (
    set "FILE_SIZE=%%~zA"
    set /a FILE_SIZE_KB=!FILE_SIZE!/1024
    echo File size: !FILE_SIZE_KB! KB
)

echo.
echo [INFO] Backup completed at: %date% %time%
echo.
echo Press any key to exit...
pause
