@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   启动开发环境（前端 + 后端）
echo ========================================
echo.
echo 项目路径: %CD%
echo 注意：此脚本仅启动前后端服务，Docker服务请手动启动
echo.

REM 检查是否在项目根目录
if not exist "backend\pom.xml" (
    echo ❌ 错误: 未找到 backend\pom.xml
    echo 当前目录: %CD%
    echo 请确保脚本在项目根目录下
    pause
    exit /b 1
)

if not exist "frontend\package.json" (
    echo ❌ 错误: 未找到 frontend\package.json
    echo 当前目录: %CD%
    echo 请确保脚本在项目根目录下
    pause
    exit /b 1
)

echo [1/5] 检查 Java 环境...
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

echo [2/5] 检查 Node.js 环境...
where node >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Node.js，请先安装 Node.js 16+
    echo.
    echo 请检查：
    echo   1. Node.js 是否已安装
    echo   2. PATH 中是否包含 Node.js 目录
    echo.
    pause
    exit /b 1
)

REM 检查Node版本
for /f "tokens=1" %%v in ('node --version 2^>nul') do set NODE_VERSION=%%v
echo ✅ Node.js 环境正常: !NODE_VERSION!
echo.

echo [3/5] 检查后端JAR文件...
if not exist "backend\target\iot-platform.jar" (
    echo ❌ 错误: 后端JAR文件不存在
    echo 请先手动编译后端: cd backend ^&^& mvn package -DskipTests
    echo.
    pause
    exit /b 1
) else (
    echo ✅ 后端JAR文件存在
)
echo.

echo [4/5] 检查并安装前端依赖...
if not exist "frontend\node_modules" (
    echo ⚠️  前端依赖未安装，正在安装（这可能需要几分钟）...
    cd frontend
    call npm install
    if errorlevel 1 (
        echo ❌ 前端依赖安装失败
        cd ..
        pause
        exit /b 1
    )
    cd ..
    echo ✅ 前端依赖安装完成
) else (
    echo ✅ 前端依赖已安装
)
echo.

echo [5/5] 启动服务...
echo.

REM 检查端口是否被占用
netstat -an | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  警告: 端口 8080 已被占用，后端可能无法启动
    echo.
)

netstat -an | findstr ":5173" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo ⚠️  警告: 端口 5173 已被占用，前端可能无法启动
    echo.
)

REM 启动后端（新窗口）
echo 正在启动后端服务（端口 8080）...
REM 创建临时启动脚本（避免引号嵌套问题）
echo @echo off > "%TEMP%\start-backend-temp.bat"
echo setlocal enabledelayedexpansion >> "%TEMP%\start-backend-temp.bat"
echo cd /d "%CD%\backend" >> "%TEMP%\start-backend-temp.bat"
echo set "JAVA_HOME=!JAVA_HOME_PATH!" >> "%TEMP%\start-backend-temp.bat"
echo set "PATH=!JAVA_HOME!\bin;%%PATH%%" >> "%TEMP%\start-backend-temp.bat"
echo "!JAVA_HOME!\bin\java.exe" -jar target\iot-platform.jar >> "%TEMP%\start-backend-temp.bat"
echo pause >> "%TEMP%\start-backend-temp.bat"
start "IoT Platform - Backend" cmd /k "%TEMP%\start-backend-temp.bat"
echo ✅ 后端服务已启动（新窗口）
echo.

REM 等待后端启动
echo 等待后端启动（10秒）...
timeout /t 10 /nobreak >nul
echo.

REM 启动前端（新窗口）
echo 正在启动前端服务（端口 5173）...
REM 创建临时启动脚本（避免引号嵌套问题）
echo @echo off > "%TEMP%\start-frontend-temp.bat"
echo setlocal enabledelayedexpansion >> "%TEMP%\start-frontend-temp.bat"
echo cd /d "%CD%\frontend" >> "%TEMP%\start-frontend-temp.bat"
echo npm run dev >> "%TEMP%\start-frontend-temp.bat"
echo pause >> "%TEMP%\start-frontend-temp.bat"
start "IoT Platform - Frontend" cmd /k "%TEMP%\start-frontend-temp.bat"
echo ✅ 前端服务已启动（新窗口）
echo.

echo ========================================
echo   ✅ 启动完成
echo ========================================
echo.
echo 服务地址:
echo   - 前端界面: http://localhost:5173
echo   - 后端API:   http://localhost:8080
echo   - API文档:   http://localhost:8080/swagger-ui.html
echo.
echo 项目路径: %CD%
echo.
echo 注意:
echo   - 后端和前端都在独立窗口中运行
echo   - 关闭窗口即可停止对应服务
echo   - 确保 Docker 服务（MySQL、Redis、EMQX）已手动启动
echo   - 如果服务启动失败，请检查对应窗口的错误信息
echo   - 临时启动脚本已生成在 %TEMP% 目录，可手动删除
echo.
pause
endlocal