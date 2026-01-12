@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Frontend Build
echo ========================================
echo.
echo Project: %CD%
echo.

REM 检查 Node.js 是否安装
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not installed
    echo Please install Node.js: https://nodejs.org/
    pause
    exit /b 1
)

REM 显示 Node.js 版本
for /f "tokens=*" %%v in ('node -v') do echo [INFO] Node.js version: %%v

REM 检查前端目录是否存在
if not exist "frontend" (
    echo [ERROR] frontend directory not found
    pause
    exit /b 1
)

REM 切换到前端目录
cd frontend

REM 检查 package.json 是否存在
if not exist "package.json" (
    echo [ERROR] package.json not found
    pause
    exit /b 1
)

REM 检查 node_modules 是否存在
if not exist "node_modules" (
    echo [INFO] node_modules not found, installing dependencies...
    echo.
    npm install
    if errorlevel 1 (
        echo [ERROR] npm install failed
        pause
        exit /b 1
    )
    echo [OK] Dependencies installed
    echo.
)

REM 执行编译
echo [INFO] Building frontend...
echo.
npm run build

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
echo Output: frontend\dist\
echo.

REM 检查 dist 目录
if exist "dist\index.html" (
    echo [OK] dist\index.html exists
) else (
    echo [WARNING] dist\index.html not found
)

echo.
echo Next steps:
echo   - Local test: npm run dev
echo   - Deploy: scripts\deploy-frontend.bat
echo.
pause
endlocal
