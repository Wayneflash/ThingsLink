@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   编译后端JAR包
echo ========================================
echo.
echo 项目路径: %CD%
echo.

REM 检查是否在项目根目录
if not exist "backend\pom.xml" (
    echo ❌ 错误: 未找到 backend\pom.xml
    echo 当前目录: %CD%
    echo 请确保脚本在项目根目录下
    pause
    exit /b 1
)

echo [1/4] 检查 Java 环境...
set JAVA_FOUND=0
set JAVA_EXE=
set JAVA_HOME_PATH=

REM 方法1: 检查PATH中是否有java
where java >nul 2>&1
if not errorlevel 1 (
    set JAVA_FOUND=1
    set JAVA_EXE=java
    for /f "tokens=*" %%i in ('where java') do (
        set JAVA_HOME_PATH=%%~dpi
        set JAVA_HOME_PATH=!JAVA_HOME_PATH:~0,-5!
    )
)

REM 方法2: 检查JAVA_HOME环境变量
if !JAVA_FOUND!==0 (
    if defined JAVA_HOME (
        if exist "!JAVA_HOME!\bin\java.exe" (
            set JAVA_FOUND=1
            set JAVA_EXE="!JAVA_HOME!\bin\java.exe"
            set JAVA_HOME_PATH=!JAVA_HOME!
        )
    )
)

REM 方法3: 查找常见的Java安装路径 (Eclipse Adoptium)
if !JAVA_FOUND!==0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe" (
        set JAVA_FOUND=1
        set JAVA_EXE="C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe"
        set JAVA_HOME_PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
    )
)

if !JAVA_FOUND!==0 (
    echo ❌ 错误: 未找到 Java，请先安装 JDK 17+
    echo.
    echo 请检查：
    echo   1. Java 是否已安装
    echo   2. JAVA_HOME 环境变量是否设置
    echo   3. PATH 中是否包含 Java bin 目录
    echo.
    pause
    exit /b 1
)

REM 检查Java版本
if "!JAVA_EXE!"=="java" (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%v
        set JAVA_VERSION=!JAVA_VERSION:"=!
        goto :java_version_ok
    )
) else (
    for /f "tokens=3" %%v in ('!JAVA_EXE! -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%v
        set JAVA_VERSION=!JAVA_VERSION:"=!
        goto :java_version_ok
    )
)
:java_version_ok
echo ✅ Java 环境正常: !JAVA_VERSION!
echo    Java路径: !JAVA_HOME_PATH!
echo.

REM 设置JAVA_HOME和PATH，确保Maven能找到
set "JAVA_HOME=!JAVA_HOME_PATH!"
set "PATH=!JAVA_HOME!\bin;!PATH!"

echo [2/4] 检查 Maven 环境...
where mvn >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Maven，请先安装 Maven
    echo.
    echo 请检查：
    echo   1. Maven 是否已安装
    echo   2. PATH 中是否包含 Maven bin 目录
    echo.
    pause
    exit /b 1
)

REM 检查Maven版本
for /f "tokens=3" %%v in ('mvn --version 2^>nul ^| findstr /i "Apache Maven"') do (
    set MAVEN_VERSION=%%v
    goto :maven_version_ok
)
:maven_version_ok
echo ✅ Maven 环境正常: !MAVEN_VERSION!
echo.

echo [3/4] 清理旧的编译文件...
cd backend

REM 检查jar文件是否被占用（后端服务可能正在运行）
if exist "target\iot-platform.jar" (
    echo [INFO] 检测到旧的jar文件，正在检查是否被占用...
    
    REM 尝试删除旧的jar文件
    del /f /q "target\iot-platform.jar" >nul 2>&1
    if errorlevel 1 (
        echo ⚠️  警告: 无法删除旧的jar文件，可能后端服务正在运行
        echo.
        echo 请先停止后端服务：
        echo   1. 关闭运行后端的命令行窗口
        echo   2. 或使用任务管理器结束 java.exe 进程
        echo   3. 然后重新运行此脚本
        echo.
        cd ..
        pause
        exit /b 1
    )
    echo ✅ 已删除旧的jar文件
)

REM 删除.original文件（如果存在）
if exist "target\iot-platform.jar.original" (
    del /f /q "target\iot-platform.jar.original" >nul 2>&1
)

call mvn clean >nul 2>&1
cd ..
echo ✅ 清理完成
echo.

echo [4/4] 编译后端（这可能需要几分钟）...
cd backend
call mvn package -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ 后端编译失败
    echo 请检查编译错误信息
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ✅ 后端编译完成！
echo.

REM 检查编译结果
if exist "backend\target\iot-platform.jar" (
    for %%A in ("backend\target\iot-platform.jar") do set JAR_SIZE=%%~zA
    set /a JAR_SIZE_MB=!JAR_SIZE!/1024/1024
    echo [INFO] JAR文件位置: backend\target\iot-platform.jar
    echo [INFO] JAR文件大小: !JAR_SIZE_MB! MB
    echo.
    echo ========================================
    echo   ✅ 编译成功
    echo ========================================
    echo.
    echo 下一步：
    echo   1. 使用 scripts\deploy-backend.bat 部署到服务器
    echo   2. 或手动上传 backend\target\iot-platform.jar
    echo.
) else (
    echo ❌ 错误: 编译完成但未找到 jar 文件
    pause
    exit /b 1
)

pause
endlocal
