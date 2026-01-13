@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory (auto adapt different paths)
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Backend Build
echo ========================================
echo.
echo Project: %CD%
echo.

REM Check Java installation (prefer JAVA_HOME)
set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "!JAVA_HOME!\bin\java.exe" (
        set "JAVA_CMD=!JAVA_HOME!\bin\java.exe"
    )
)

REM Check if Java is available (use quotes to handle spaces)
if exist "!JAVA_CMD!" (
    "!JAVA_CMD!" -version >nul 2>&1
    if errorlevel 1 (
        REM If direct path fails, try java command
        java -version >nul 2>&1
        if errorlevel 1 (
            echo [ERROR] Java not installed or not in PATH
            echo Please install JDK 11+ and set JAVA_HOME
            echo Download: https://adoptium.net/
            pause
            exit /b 1
        )
        set "JAVA_CMD=java"
    )
) else (
    REM If JAVA_HOME path doesn't exist, try java in PATH
    java -version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Java not installed or not in PATH
        echo Please install JDK 11+ and set JAVA_HOME
        echo Download: https://adoptium.net/
        pause
        exit /b 1
    )
    set "JAVA_CMD=java"
)

REM Display Java version (use temp file to avoid quote issues)
set "TEMP_FILE=%TEMP%\java_version_%RANDOM%.txt"
"!JAVA_CMD!" -version > "!TEMP_FILE!" 2>&1
for /f "tokens=3" %%v in ('type "!TEMP_FILE!" ^| findstr /i "version"') do (
    echo [INFO] Java version: %%v
    goto :java_done
)
:java_done
del /q "!TEMP_FILE!" >nul 2>&1
if defined JAVA_HOME (
    echo [INFO] JAVA_HOME: !JAVA_HOME!
) else (
    echo [INFO] JAVA_HOME: (not set, using PATH)
)

REM Check if Maven is installed
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not installed
    echo Please install Maven: https://maven.apache.org/
    pause
    exit /b 1
)

REM Display Maven version
for /f "tokens=3" %%v in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do (
    echo [INFO] Maven version: %%v
    goto :mvn_done
)
:mvn_done

REM Check if backend directory exists
if not exist "backend" (
    echo [ERROR] backend directory not found
    pause
    exit /b 1
)

REM Change to backend directory
cd backend

REM Check if pom.xml exists
if not exist "pom.xml" (
    echo [ERROR] pom.xml not found
    pause
    exit /b 1
)

echo.
echo [INFO] Building backend (clean + package)...
echo.

REM Execute build
mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Build Success!
echo ========================================
echo.
echo Output: backend\target\iot-platform.jar
echo.

REM Check jar file
if exist "target\iot-platform.jar" (
    for %%F in (target\iot-platform.jar) do (
        echo [OK] JAR file size: %%~zF bytes
    )
) else (
    echo [WARNING] iot-platform.jar not found
)

echo.
echo Next steps:
echo   - Local test: java -jar target\iot-platform.jar
echo   - Deploy: scripts\deploy-backend.bat
echo.
pause
endlocal
