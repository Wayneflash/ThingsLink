@echo off
setlocal enabledelayedexpansion
echo ========================================
echo   Start All Services (Docker+Backend+Frontend)
echo ========================================
echo.

REM Auto detect script directory
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
if errorlevel 1 (
    echo [ERROR] Cannot change to script directory: %SCRIPT_DIR%
    pause
    exit /b 1
)

echo [1/4] Starting Docker services (auto restore database)...
REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running, please start Docker Desktop first
    echo Waiting for Docker to start...
    timeout /t 5 /nobreak >nul
    
    REM Check again
    docker info >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker still not running, please start Docker Desktop manually
        echo Skipping Docker startup, continuing with other steps...
        goto :skip_docker
    )
)

echo [OK] Docker is running

echo [INFO] Starting Docker containers...
docker-compose up -d >nul 2>&1

if errorlevel 1 (
    echo [WARNING] Docker startup failed, continuing with other steps...
    goto :skip_docker
)

echo [INFO] Waiting for MySQL to start...
timeout /t 5 /nobreak >nul

REM Wait for MySQL to be ready
:wait_mysql
docker exec iot-mysql mysqladmin ping -uroot -proot123456 --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :wait_mysql
)
echo [OK] MySQL is ready

REM Check and restore database
set "BACKUP_FILE=%CD%\backups\iot_platform_latest.sql"
if exist "%BACKUP_FILE%" (
    echo [INFO] Found database backup file, checking database status...
    
    REM Check if database is empty
    for /f "tokens=*" %%i in ('docker exec iot-mysql mysql -uroot -proot123456 -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='iot_platform';" -s -N 2^>nul') do set TABLE_COUNT=%%i
    
    if "%TABLE_COUNT%"=="" set TABLE_COUNT=0
    if %TABLE_COUNT% LSS 5 (
        echo [INFO] Database is empty, restoring backup...
        docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < "%BACKUP_FILE%" >nul 2>&1
        if not errorlevel 1 (
            echo [OK] Database restored successfully!
        ) else (
            echo [WARNING] Database restore failed, but services are started
        )
    ) else (
        echo [INFO] Database already has data, skipping restore
    )
) else (
    echo [INFO] Backup file not found, using initial database
    echo Backup file path: %BACKUP_FILE%
)

echo [OK] Docker services started

:skip_docker

echo.
echo [2/4] Starting backend service...
set "BACKEND_DIR=%CD%\backend"

REM Set Java environment variable
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot" (
        set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set JAVA_HOME=C:\Program Files\Java\jdk-17
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

REM Stop old 8080 port process
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

if exist "%BACKEND_DIR%\target\iot-platform.jar" (
    cd /d "%BACKEND_DIR%"
    if errorlevel 1 (
        echo [WARNING] Cannot change to backend directory: %BACKEND_DIR%
    ) else (
        start "IOT Backend" cmd /k "java -jar target\iot-platform.jar"
        if errorlevel 1 (
            echo [WARNING] Failed to start backend service
        ) else (
            echo [OK] Backend service started
        )
    )
) else (
    echo [WARNING] Backend jar file not found, skipping backend startup
    echo Please compile first: cd backend ^&^& mvn clean package -DskipTests
)

echo.
echo [3/4] Starting frontend service...
set "FRONTEND_DIR=%CD%\frontend"

REM Stop old 5173 port process
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

if exist "%FRONTEND_DIR%\package.json" (
    cd /d "%FRONTEND_DIR%"
    if errorlevel 1 (
        echo [WARNING] Cannot change to frontend directory: %FRONTEND_DIR%
    ) else (
        REM Check dependencies
        if not exist "node_modules" (
            echo [INFO] Installing frontend dependencies...
            call npm install >nul 2>&1
        )
        
        start "IOT Frontend" cmd /k "npm run dev"
        if errorlevel 1 (
            echo [WARNING] Failed to start frontend service
        ) else (
            echo [OK] Frontend service started
        )
    )
) else (
    echo [WARNING] Frontend directory not found, skipping frontend startup
)

echo.
echo [4/4] Completed!

echo.
echo ========================================
echo   All Services Started!
echo ========================================
echo.
echo Service addresses:
echo   MySQL:    localhost:3306
echo   Redis:    localhost:16379
echo   EMQX:     localhost:1883 (MQTT)
echo   EMQX Web: http://localhost:18083 (admin/public)
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo.
pause
