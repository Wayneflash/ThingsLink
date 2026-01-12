@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Backend Build
echo ========================================
echo.
echo Project: %CD%
echo.

REM 检查 Java 是否安装（优先使用JAVA_HOME）
set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "!JAVA_HOME!\bin\java.exe" (
        set "JAVA_CMD=!JAVA_HOME!\bin\java.exe"
    )
)

"!JAVA_CMD!" -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not installed or not in PATH
    echo Please install JDK 11+ and set JAVA_HOME
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

REM 显示 Java 版本
for /f "tokens=3" %%v in ('"!JAVA_CMD!" -version 2^>^&1 ^| findstr /i "version"') do (
    echo [INFO] Java version: %%v
    goto :java_done
)
:java_done
echo [INFO] JAVA_HOME: !JAVA_HOME!

REM 检查 Maven 是否安装
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not installed
    echo Please install Maven: https://maven.apache.org/
    pause
    exit /b 1
)

REM 显示 Maven 版本
for /f "tokens=3" %%v in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do (
    echo [INFO] Maven version: %%v
    goto :mvn_done
)
:mvn_done

REM 检查后端目录是否存在
if not exist "backend" (
    echo [ERROR] backend directory not found
    pause
    exit /b 1
)

REM 切换到后端目录
cd backend

REM 检查 pom.xml 是否存在
if not exist "pom.xml" (
    echo [ERROR] pom.xml not found
    pause
    exit /b 1
)

echo.
echo [INFO] Building backend (clean + package)...
echo.

REM 执行编译
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

REM 检查 jar 文件
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
