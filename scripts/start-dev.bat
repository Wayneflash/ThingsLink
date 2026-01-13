@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM Get script directory (auto adapt different paths)
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory (parent of scripts directory)
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Start Development Environment
echo ========================================
echo.
echo Project Path: %CD%
echo Note: This script only starts frontend and backend services, Docker services need to be started manually
echo.

REM Check if in project root directory
if not exist "backend\pom.xml" (
    echo [ERROR] backend\pom.xml not found
    echo Current directory: %CD%
    echo Please ensure script is in project root directory
    pause
    exit /b 1
)

if not exist "frontend\package.json" (
    echo [ERROR] frontend\package.json not found
    echo Current directory: %CD%
    echo Please ensure script is in project root directory
    pause
    exit /b 1
)

echo [1/5] Checking Java environment...
set JAVA_FOUND=0
set JAVA_EXE=
set JAVA_HOME_PATH=

REM Method 1: Check if java is in PATH
where java >nul 2>&1
if not errorlevel 1 (
    set JAVA_FOUND=1
    set JAVA_EXE=java
    for /f "tokens=*" %%i in ('where java') do (
        set JAVA_HOME_PATH=%%~dpi
        set JAVA_HOME_PATH=!JAVA_HOME_PATH:~0,-5!
    )
)

REM Method 2: Check JAVA_HOME environment variable
if !JAVA_FOUND!==0 (
    if defined JAVA_HOME (
        if exist "!JAVA_HOME!\bin\java.exe" (
            set JAVA_FOUND=1
            set JAVA_EXE="!JAVA_HOME!\bin\java.exe"
            set JAVA_HOME_PATH=!JAVA_HOME!
        )
    )
)

REM Method 3: Find common Java installation path (Eclipse Adoptium)
if !JAVA_FOUND!==0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe" (
        set JAVA_FOUND=1
        set JAVA_EXE="C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe"
        set JAVA_HOME_PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
    )
)

if !JAVA_FOUND!==0 (
    echo [ERROR] Java not found, please install JDK 17+
    echo.
    echo Please check:
    echo   1. Is Java installed?
    echo   2. Is JAVA_HOME environment variable set?
    echo   3. Does PATH contain Java bin directory?
    echo.
    pause
    exit /b 1
)

REM Check Java version
set JAVA_VERSION=
if "!JAVA_EXE!"=="java" (
    REM Use temp file to avoid quote issues
    set "TEMP_JAVA_VER=%TEMP%\java_ver_%RANDOM%.txt"
    java -version > "!TEMP_JAVA_VER!" 2>&1
    for /f "tokens=3" %%v in ('type "!TEMP_JAVA_VER!" ^| findstr /i "version"') do (
        set JAVA_VERSION=%%v
        set JAVA_VERSION=!JAVA_VERSION:"=!
        goto :java_version_ok
    )
    del /q "!TEMP_JAVA_VER!" >nul 2>&1
) else (
    REM Use temp file to avoid quote issues with paths containing spaces
    set "TEMP_JAVA_VER=%TEMP%\java_ver_%RANDOM%.txt"
    !JAVA_EXE! -version > "!TEMP_JAVA_VER!" 2>&1
    for /f "tokens=3" %%v in ('type "!TEMP_JAVA_VER!" ^| findstr /i "version"') do (
        set JAVA_VERSION=%%v
        set JAVA_VERSION=!JAVA_VERSION:"=!
        goto :java_version_ok
    )
    del /q "!TEMP_JAVA_VER!" >nul 2>&1
)
:java_version_ok
if defined JAVA_VERSION (
    echo [OK] Java environment OK: !JAVA_VERSION!
) else (
    echo [OK] Java environment OK
)
echo    Java path: !JAVA_HOME_PATH!
echo.

REM Set JAVA_HOME and PATH to ensure Maven can find it
set "JAVA_HOME=!JAVA_HOME_PATH!"
if "!JAVA_EXE!"=="java" (
    REM Java is in PATH, no need to modify PATH
) else (
    REM Add Java bin to PATH (handle spaces in path)
    set "PATH=!JAVA_HOME!\bin;!PATH!"
)

echo [2/5] Checking Node.js environment...
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found, please install Node.js 16+
    echo.
    echo Please check:
    echo   1. Is Node.js installed?
    echo   2. Does PATH contain Node.js directory?
    echo.
    pause
    exit /b 1
)

REM Check Node version
for /f "tokens=1" %%v in ('node --version 2^>nul') do set NODE_VERSION=%%v
echo [OK] Node.js environment OK: !NODE_VERSION!
echo.

echo [3/5] Checking backend JAR file...
if not exist "backend\target\iot-platform.jar" (
    echo [ERROR] Backend JAR file does not exist
    echo Please compile backend manually: cd backend ^&^& mvn package -DskipTests
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Backend JAR file exists
)
echo.

echo [4/5] Checking and installing frontend dependencies...
if not exist "frontend\node_modules" (
    echo [WARNING] Frontend dependencies not installed, installing...
    echo This may take a few minutes...
    cd frontend
    call npm install
    if errorlevel 1 (
        echo [ERROR] Frontend dependency installation failed
        cd ..
        pause
        exit /b 1
    )
    cd ..
    echo [OK] Frontend dependencies installed
) else (
    echo [OK] Frontend dependencies already installed
)
echo.

echo [5/5] Starting services...
echo.

REM Check if ports are occupied
netstat -an | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Port 8080 is occupied, backend may not start
    echo.
)

netstat -an | findstr ":5173" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Port 5173 is occupied, frontend may not start
    echo.
)

REM Start backend (new window)
echo Starting backend service (port 8080)...
REM Create temporary startup script (handle paths with spaces)
set "TEMP_BACKEND=%TEMP%\start-backend-temp.bat"
echo @echo off > "!TEMP_BACKEND!"
echo setlocal enabledelayedexpansion >> "!TEMP_BACKEND!"
echo cd /d "%CD%\backend" >> "!TEMP_BACKEND!"
echo set "JAVA_HOME=!JAVA_HOME_PATH!" >> "!TEMP_BACKEND!"
echo set "PATH=!JAVA_HOME!\bin;%%PATH%%" >> "!TEMP_BACKEND!"
REM Write Java command based on JAVA_EXE (avoid if-else in echo redirection)
set "JAVA_CMD_LINE="
if "!JAVA_EXE!"=="java" (
    set "JAVA_CMD_LINE=java -jar target\iot-platform.jar"
) else (
    set "JAVA_CMD_LINE=!JAVA_EXE! -jar target\iot-platform.jar"
)
echo !JAVA_CMD_LINE! >> "!TEMP_BACKEND!"
echo pause >> "!TEMP_BACKEND!"
start "IoT Platform - Backend" cmd /k "!TEMP_BACKEND!"
echo [OK] Backend service started (new window)
echo.

REM Wait for backend to start
echo Waiting for backend to start (10 seconds)...
timeout /t 10 /nobreak >nul
echo.

REM Start frontend (new window)
echo Starting frontend service (port 5173)...
REM Create temporary startup script
set "TEMP_FRONTEND=%TEMP%\start-frontend-temp.bat"
echo @echo off > "!TEMP_FRONTEND!"
echo setlocal enabledelayedexpansion >> "!TEMP_FRONTEND!"
echo cd /d "%CD%\frontend" >> "!TEMP_FRONTEND!"
echo npm run dev >> "!TEMP_FRONTEND!"
echo pause >> "!TEMP_FRONTEND!"
start "IoT Platform - Frontend" cmd /k "!TEMP_FRONTEND!"
echo [OK] Frontend service started (new window)
echo.

echo ========================================
echo   [OK] Startup Complete
echo ========================================
echo.
echo Service URLs:
echo   - Frontend: http://localhost:5173
echo   - Backend API:   http://localhost:8080
echo   - API Docs:   http://localhost:8080/swagger-ui.html
echo.
echo Project Path: %CD%
echo.
echo Notes:
echo   - Backend and frontend run in separate windows
echo   - Close windows to stop corresponding services
echo   - Ensure Docker services (MySQL, Redis, EMQX) are started manually
echo   - If services fail to start, check error messages in corresponding windows
echo   - Temporary startup scripts are generated in %TEMP% directory, can be deleted manually
echo.
pause
endlocal
