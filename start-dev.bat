@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   启动开发环境（前端 + 后端）
echo ========================================
echo.
echo 注意：此脚本仅启动前后端服务，Docker服务请手动启动
echo.

REM 检查是否在项目根目录
if not exist "backend\pom.xml" (
    echo ❌ 错误: 未找到 backend\pom.xml
    echo 请确保在项目根目录下运行此脚本
    pause
    exit /b 1
)

if not exist "frontend\package.json" (
    echo ❌ 错误: 未找到 frontend\package.json
    echo 请确保在项目根目录下运行此脚本
    pause
    exit /b 1
)

echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Java，请先安装 JDK 11+
    pause
    exit /b 1
)
echo ✅ Java 环境正常
echo.

echo [2/4] 检查 Node.js 环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Node.js，请先安装 Node.js 16+
    pause
    exit /b 1
)
echo ✅ Node.js 环境正常
echo.

echo [3/4] 启动后端服务...
echo 正在启动 Spring Boot 后端（端口 8080）...
echo.

REM 检查后端是否已编译
if not exist "backend\target\iot-platform.jar" (
    echo ⚠️  后端未编译，正在编译...
    cd backend
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ❌ 后端编译失败
        pause
        exit /b 1
    )
    cd ..
    echo ✅ 后端编译完成
    echo.
)

REM 启动后端（新窗口）
start "IoT Platform - Backend" cmd /k "cd backend && java -jar target\iot-platform.jar"
echo ✅ 后端服务已启动（新窗口）
echo.

REM 等待后端启动
echo 等待后端启动（10秒）...
timeout /t 10 /nobreak >nul
echo.

echo [4/4] 启动前端服务...
echo 正在启动 Vue 前端开发服务器（端口 5173）...
echo.

REM 检查前端依赖是否已安装
if not exist "frontend\node_modules" (
    echo ⚠️  前端依赖未安装，正在安装...
    cd frontend
    call npm install
    if errorlevel 1 (
        echo ❌ 前端依赖安装失败
        pause
        exit /b 1
    )
    cd ..
    echo ✅ 前端依赖安装完成
    echo.
)

REM 启动前端（新窗口）
start "IoT Platform - Frontend" cmd /k "cd frontend && npm run dev"
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
echo 注意:
echo   - 后端和前端都在独立窗口中运行
echo   - 关闭窗口即可停止对应服务
echo   - 确保 Docker 服务（MySQL、Redis、EMQX）已手动启动
echo.
pause
