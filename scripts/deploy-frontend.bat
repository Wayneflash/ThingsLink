@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   部署前端Dist到服务器
echo ========================================
echo.
echo 项目路径: %CD%
echo.

REM 检查配置文件
if not exist "scripts\deploy-config.txt" (
    echo [ERROR] 配置文件 scripts\deploy-config.txt 不存在
    echo 请先创建配置文件
    pause
    exit /b 1
)

REM 读取配置（简化版）
set REMOTE_HOST=
set REMOTE_PORT=22
set REMOTE_USER=root
set REMOTE_FRONTEND_PATH=/opt/iot-platform/frontend
set LOCAL_FRONTEND_DIST=frontend\dist

for /f "eol=# tokens=1,* delims==" %%a in (scripts\deploy-config.txt) do (
    if "%%a"=="REMOTE_HOST" set REMOTE_HOST=%%b
    if "%%a"=="REMOTE_PORT" set REMOTE_PORT=%%b
    if "%%a"=="REMOTE_USER" set REMOTE_USER=%%b
    if "%%a"=="REMOTE_FRONTEND_PATH" set REMOTE_FRONTEND_PATH=%%b
    if "%%a"=="LOCAL_FRONTEND_DIST" set LOCAL_FRONTEND_DIST=%%b
)

REM 检查必需配置
if "!REMOTE_HOST!"=="" (
    echo [ERROR] REMOTE_HOST 未配置
    pause
    exit /b 1
)

echo 配置信息:
echo   服务器: !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_PORT!
echo   远程路径: !REMOTE_FRONTEND_PATH!
echo   本地Dist: !LOCAL_FRONTEND_DIST!
echo.

REM 检查本地dist目录
echo [1/5] 检查本地文件...
if not exist "!LOCAL_FRONTEND_DIST!" (
    echo [ERROR] dist目录不存在: !LOCAL_FRONTEND_DIST!
    echo 请先运行 scripts\build-frontend.bat 编译
    pause
    exit /b 1
)
if not exist "!LOCAL_FRONTEND_DIST!\index.html" (
    echo [ERROR] dist目录中未找到 index.html
    echo 请先运行 scripts\build-frontend.bat 编译
    pause
    exit /b 1
)
echo [OK] dist目录存在
echo.

REM 检查SSH工具
echo [2/5] 检查SSH工具...
where ssh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未找到ssh命令，请安装OpenSSH
    pause
    exit /b 1
)
where scp >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未找到scp命令，请安装OpenSSH
    pause
    exit /b 1
)
echo [OK] SSH工具可用
echo.

REM 创建压缩包
echo [3/5] 打包前端文件...
cd frontend
if exist "..\frontend-dist.tar.gz" del /f /q "..\frontend-dist.tar.gz" >nul 2>&1
tar -czf ..\frontend-dist.tar.gz dist\*
if errorlevel 1 (
    echo [ERROR] 打包失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo [OK] 打包完成
echo.

REM 上传压缩包
echo [4/5] 上传前端文件（需要输入密码）...
scp -P !REMOTE_PORT! frontend-dist.tar.gz !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_FRONTEND_PATH!/frontend-dist.tar.gz
if errorlevel 1 (
    echo [ERROR] 上传失败
    del frontend-dist.tar.gz 2>nul
    pause
    exit /b 1
)
echo [OK] 上传成功
echo.

REM 远程解压
echo [5/5] 解压前端文件（需要输入密码）...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "cd !REMOTE_FRONTEND_PATH! && rm -rf dist && tar -xzf frontend-dist.tar.gz && rm -f frontend-dist.tar.gz"
if errorlevel 1 (
    echo [WARNING] 解压可能失败，请手动检查
) else (
    echo [OK] 解压成功
)
del frontend-dist.tar.gz 2>nul
echo.

echo ========================================
echo   部署完成！
echo ========================================
echo.
echo 前端路径: !REMOTE_FRONTEND_PATH!/dist
echo.
pause
endlocal
