@echo off
chcp 65001 >nul
echo ========================================
echo Database Migration Script
echo ========================================
echo.
echo This script will add alarm_config and alarm_enabled fields to tb_device table.
echo.
echo Please choose your MySQL connection method:
echo.
echo 1. MySQL Command Line (if mysql is in PATH)
echo 2. Docker MySQL Container
echo 3. Manual execution (copy SQL to your MySQL client)
echo.
set /p choice="Enter your choice (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo Executing SQL via MySQL command line...
    mysql -u root -p iot_platform < fix_database.sql
    if %errorlevel%==0 (
        echo.
        echo ✓ Migration completed successfully!
    ) else (
        echo.
        echo ✗ Migration failed. Please check your MySQL connection.
    )
) else if "%choice%"=="2" (
    echo.
    echo Please enter your Docker MySQL container name:
    set /p container="Container name: "
    echo.
    echo Executing SQL via Docker...
    docker exec -i %container% mysql -u root -proot123456 iot_platform < fix_database.sql
    if %errorlevel%==0 (
        echo.
        echo ✓ Migration completed successfully!
    ) else (
        echo.
        echo ✗ Migration failed. Please check your Docker container name and password.
    )
) else if "%choice%"=="3" (
    echo.
    echo Please copy the following SQL and execute it in your MySQL client:
    echo.
    echo ========================================
    type fix_database.sql
    echo ========================================
    echo.
    echo Or open fix_database.sql file and execute it.
) else (
    echo Invalid choice!
)

echo.
pause
