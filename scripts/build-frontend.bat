@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   编译前端Dist包
echo ========================================
echo.
echo 项目路径: %CD%
echo.

REM 检查是否在项目根目录
if not exist "frontend\package.json" (
    echo ❌ 错误: 未找到 frontend\package.json
    echo 当前目录: %CD%
    echo 请确保脚本在项目根目录下
    pause
    exit /b 1
)

echo [1/4] 检查 Node.js 环境...
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

echo [2/4] 检查 npm 环境...
where npm >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 npm
    pause
    exit /b 1
)

REM 检查npm版本
for /f "tokens=1" %%v in ('npm --version 2^>nul') do set NPM_VERSION=%%v
echo ✅ npm 环境正常: !NPM_VERSION!
echo.

echo [3/4] 检查并安装前端依赖...
cd frontend

if not exist "node_modules" (
    echo ⚠️  前端依赖未安装，正在安装（这可能需要几分钟）...
    call npm install
    if errorlevel 1 (
        echo ❌ 前端依赖安装失败
        cd ..
        pause
        exit /b 1
    )
    echo ✅ 前端依赖安装完成
    echo.
) else (
    echo ✅ 前端依赖已安装
    echo.
)

echo [4/4] 编译前端（这可能需要几分钟）...
call npm run build
if errorlevel 1 (
    echo.
    echo ❌ 前端编译失败
    echo 请检查编译错误信息
    cd ..
    pause
    exit /b 1
)

cd ..

echo.
echo ✅ 前端编译完成！
echo.

REM 检查编译结果
if exist "frontend\dist\index.html" (
    echo [INFO] Dist目录位置: frontend\dist
    echo.
    
    REM 统计文件数量
    set FILE_COUNT=0
    for /r "frontend\dist" %%f in (*) do set /a FILE_COUNT+=1
    
    REM 计算目录大小
    set TOTAL_SIZE=0
    for /r "frontend\dist" %%f in (*) do (
        set /a TOTAL_SIZE+=%%~zf
    )
    set /a TOTAL_SIZE_MB=!TOTAL_SIZE!/1024/1024
    
    echo [INFO] 文件数量: !FILE_COUNT! 个文件
    echo [INFO] 目录大小: !TOTAL_SIZE_MB! MB
    echo.
    echo ========================================
    echo   ✅ 编译成功
    echo ========================================
    echo.
    echo 下一步：
    echo   1. 使用 scripts\deploy-frontend.bat 部署到服务器
    echo   2. 或手动上传 frontend\dist 目录
    echo.
) else (
    echo ❌ 错误: 编译完成但未找到 dist 目录
    pause
    exit /b 1
)

pause
endlocal
