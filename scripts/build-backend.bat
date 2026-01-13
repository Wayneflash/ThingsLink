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

REM 检查Java是否可用（使用引号包裹路径，避免空格问题）
if exist "!JAVA_CMD!" (
    "!JAVA_CMD!" -version >nul 2>&1
    if errorlevel 1 (
        REM 如果直接路径失败，尝试使用java命令
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
    REM 如果JAVA_HOME路径不存在，尝试使用PATH中的java
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

REM 显示 Java 版本（使用临时文件避免引号问题）
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
