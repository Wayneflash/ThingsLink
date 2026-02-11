@echo off
chcp 65001 >nul

cd /d "%~dp0.."

echo ========================================
echo   Start Development Environment
echo ========================================
echo.
echo Project Path: %CD%
echo.

if not exist "backend\pom.xml" (
    echo [ERROR] backend\pom.xml not found
    echo Current directory: %CD%
    pause
    exit /b 1
)

if not exist "frontend\package.json" (
    echo [ERROR] frontend\package.json not found
    echo Current directory: %CD%
    pause
    exit /b 1
)

echo [1/5] Checking Java...
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%i in ('where java 2^>nul') do set "JAVA_EXE=%%i" & goto :java_found
:java_found
if not defined JAVA_EXE (
    echo [ERROR] Java not found. Set JAVA_HOME or add Java to PATH
    pause
    exit /b 1
)
echo [OK] Java: %JAVA_EXE%
echo.

echo [2/5] Checking Node.js...
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not found. Install Node.js 16+
    pause
    exit /b 1
)
for /f "tokens=1" %%v in ('node --version 2^>nul') do set NODE_VER=%%v
echo [OK] Node %NODE_VER%
echo.

echo [3/5] Checking backend JAR...
if not exist "backend\target\iot-platform.jar" (
    echo [ERROR] Backend JAR not found. Run: cd backend ^&^& mvn package -DskipTests
    pause
    exit /b 1
)
echo [OK] JAR exists
echo.

echo [4/5] Checking frontend...
if not exist "frontend\node_modules" (
    echo Installing frontend deps...
    cd frontend
    call npm install
    cd ..
)
echo [OK] Frontend ready
echo.

echo [5/5] Starting services...

netstat -an | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 echo [WARN] Port 8081 in use

netstat -an | findstr ":5173" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 echo [WARN] Port 5173 in use

echo.
echo Starting backend on 8081...
for %%A in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpA"
set "LAUNCH_BACKEND=%TEMP%\iot-backend-start.bat"
echo @echo off > "%LAUNCH_BACKEND%"
echo cd /d "%CD%\backend" >> "%LAUNCH_BACKEND%"
echo set "PATH=%JAVA_BIN%;%%PATH%%" >> "%LAUNCH_BACKEND%"
echo "%JAVA_EXE%" -jar target\iot-platform.jar --spring.profiles.active=dev --server.port=8081 --mqtt.broker-url=tcp://localhost:11883 >> "%LAUNCH_BACKEND%"
echo pause >> "%LAUNCH_BACKEND%"
start "IoTBackend" cmd /k "%LAUNCH_BACKEND%"

echo Wait 10 sec...
ping 127.0.0.1 -n 11 >nul

echo Starting frontend on 5173...
start "IoTFrontend" cmd /k "cd /d %CD%\frontend & npm run dev"

echo.
echo ========================================
echo   Done. Frontend: http://localhost:5173
echo   Backend:  http://localhost:8081
echo ========================================
echo.
pause
