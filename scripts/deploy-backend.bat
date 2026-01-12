@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   部署后端JAR包到服务器
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

REM 读取配置（简化版，直接设置变量）
set REMOTE_HOST=
set REMOTE_PORT=22
set REMOTE_USER=root
set REMOTE_BACKEND_PATH=/opt/iot-platform/backend
set REMOTE_BACKEND_SERVICE=iot-platform
set LOCAL_BACKEND_JAR=backend\target\iot-platform.jar

for /f "eol=# tokens=1,* delims==" %%a in (scripts\deploy-config.txt) do (
    if "%%a"=="REMOTE_HOST" set REMOTE_HOST=%%b
    if "%%a"=="REMOTE_PORT" set REMOTE_PORT=%%b
    if "%%a"=="REMOTE_USER" set REMOTE_USER=%%b
    if "%%a"=="REMOTE_BACKEND_PATH" set REMOTE_BACKEND_PATH=%%b
    if "%%a"=="REMOTE_BACKEND_SERVICE" set REMOTE_BACKEND_SERVICE=%%b
    if "%%a"=="LOCAL_BACKEND_JAR" set LOCAL_BACKEND_JAR=%%b
)

REM 检查必需配置
if "!REMOTE_HOST!"=="" (
    echo [ERROR] REMOTE_HOST 未配置
    pause
    exit /b 1
)

echo 配置信息:
echo   服务器: !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_PORT!
echo   远程路径: !REMOTE_BACKEND_PATH!
echo   服务名: !REMOTE_BACKEND_SERVICE!
echo   本地JAR: !LOCAL_BACKEND_JAR!
echo.

REM 检查本地jar文件
echo [1/4] 检查本地文件...
if not exist "!LOCAL_BACKEND_JAR!" (
    echo [ERROR] jar文件不存在: !LOCAL_BACKEND_JAR!
    echo 请先运行 scripts\build-backend.bat 编译
    pause
    exit /b 1
)
echo [OK] jar文件存在
echo.

REM 检查SSH工具
echo [2/4] 检查SSH工具...
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

REM 上传jar文件
echo [3/4] 上传jar包（需要输入密码）...
scp -P !REMOTE_PORT! "!LOCAL_BACKEND_JAR!" !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_BACKEND_PATH!/iot-platform.jar
if errorlevel 1 (
    echo [ERROR] 上传失败
    pause
    exit /b 1
)
echo [OK] 上传成功
echo.

REM 重启服务
echo [4/4] 重启后端服务（需要输入密码）...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "systemctl restart !REMOTE_BACKEND_SERVICE!"
if errorlevel 1 (
    echo [WARNING] 重启服务可能失败，请手动检查
) else (
    echo [OK] 服务已重启
)
echo.

echo ========================================
echo   部署完成！
echo ========================================
echo.
pause
endlocal
