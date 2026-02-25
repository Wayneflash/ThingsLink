@echo off
chcp 65001 >nul
set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
cd /d "%SCRIPT_DIR%\.."

echo ========================================
echo   Backend (dev-remote) - 23307/26379/21183
echo ========================================
echo.
echo Make sure SSH tunnel is running: scripts\start-ssh-tunnel.bat
echo Profile: dev-remote ^(MySQL 23307, Redis 26379, MQTT 21183^)
echo.

if not exist "backend\target\iot-platform.jar" (
    echo [ERROR] backend\target\iot-platform.jar not found
    echo Run: cd backend ^&^& mvn package -DskipTests
    pause
    exit /b 1
)

set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%i in ('where java 2^>nul') do set "JAVA_EXE=%%i" & goto :got_java
:got_java
if not defined JAVA_EXE (
    echo [ERROR] Java not found
    pause
    exit /b 1
)

cd backend
"%JAVA_EXE%" -jar target\iot-platform.jar --spring.profiles.active=dev-remote --server.port=8081
pause
