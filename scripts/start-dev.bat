@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
cd /d "%SCRIPT_DIR%\.."

echo ========================================
echo   ThingsLink 1.0 - Start Dev (All-in-One)
echo ========================================
echo.
echo Project: %CD%
echo.

set USE_REMOTE_DB=1
if exist "scripts\dev-config.txt" (
    for /f "usebackq eol=# tokens=1,* delims==" %%a in ("scripts\dev-config.txt") do (
        if "%%~a"=="USE_REMOTE_DB" set "USE_REMOTE_DB=%%~b"
    )
)
set "USE_REMOTE_DB=%USE_REMOTE_DB: =%"

if "%USE_REMOTE_DB%"=="1" (
    set SPRING_PROFILES=dev-remote
    echo [CONFIG] Remote: 117.72.162.225 (MySQL/Redis/MQTT via tunnel)
) else (
    set SPRING_PROFILES=dev
    echo [CONFIG] Local Docker
)
echo.

if not exist "backend\pom.xml" (
    echo [ERROR] backend\pom.xml not found
    pause
    exit /b 1
)
if not exist "frontend\package.json" (
    echo [ERROR] frontend\package.json not found
    pause
    exit /b 1
)

echo [1/6] Java...
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%i in ('where java 2^>nul') do set "JAVA_EXE=%%i" & goto java_ok
:java_ok
if not defined JAVA_EXE (
    echo [ERROR] Java not found
    pause
    exit /b 1
)
echo       OK
echo.

echo [2/6] Node.js...
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found
    pause
    exit /b 1
)
echo       OK
echo.

echo [3/6] Backend JAR...
if not exist "backend\target\iot-platform.jar" (
    echo [ERROR] Not found. Run: cd backend ^&^& mvn package -DskipTests
    pause
    exit /b 1
)
echo       OK
echo.

echo [4/6] Frontend deps...
if not exist "frontend\node_modules" (
    echo       Installing...
    cd frontend
    call npm install
    cd ..
)
echo       OK
echo.

if "%USE_REMOTE_DB%"=="1" (
    echo [5/6] Remote DB via SSH tunnel
    echo       Please run scripts\start-ssh-tunnel.bat first and keep that window OPEN.
    echo.
) else (
    echo [5/6] Local Docker (no tunnel)
    echo.
)

echo [6/6] Backend + Frontend...
netstat -an | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 echo [WARN] 8081 in use
netstat -an | findstr ":3080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 echo [WARN] 3080 in use

for %%A in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpA"
set "LAUNCH=%TEMP%\iot-backend-start.bat"
if "%USE_REMOTE_DB%"=="1" (
    echo Backend: dev-remote ^(23307/21183^)
    (
        echo @echo off
        echo echo Backend: spring.profiles.active=dev-remote
        echo cd /d "%CD%\backend"
        echo set "PATH=%JAVA_BIN%;%%PATH%%"
        echo "%JAVA_EXE%" -jar target\iot-platform.jar --spring.profiles.active=dev-remote --server.port=8081
        echo pause
    ) > "%LAUNCH%"
) else (
    echo Backend: dev ^(local Docker^)
    (
        echo @echo off
        echo echo Backend: spring.profiles.active=dev
        echo cd /d "%CD%\backend"
        echo set "PATH=%JAVA_BIN%;%%PATH%%"
        echo "%JAVA_EXE%" -jar target\iot-platform.jar --spring.profiles.active=dev --server.port=8081
        echo pause
    ) > "%LAUNCH%"
)
start "Backend-8081" cmd /k "%LAUNCH%"

echo       Backend starting, wait 12 sec...
ping 127.0.0.1 -n 13 >nul

start "Frontend-3080" cmd /k "cd /d %CD%\frontend && npm run dev"

echo.
echo ========================================
echo   Done.
echo   Frontend: http://localhost:3080
echo   Backend:  http://localhost:8081
echo   Keep "SSH-Tunnel" window open.
echo ========================================
echo.
pause
