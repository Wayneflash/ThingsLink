@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   部署后端JAR包到服务器
echo ========================================
echo.
echo 项目路径: %CD%
echo.

REM 检查配置文件是否存在（在scripts目录下）
if not exist "scripts\deploy-config.txt" (
    echo [ERROR] 配置文件 scripts\deploy-config.txt 不存在
    echo 请先创建配置文件，参考 scripts\deploy-config.template.txt
    pause
    exit /b 1
)

REM 读取配置文件（从scripts目录）
for /f "tokens=1,* delims==" %%a in ('findstr /v "^#" scripts\deploy-config.txt') do (
    set "%%a=%%b"
)

REM 检查远程连接参数
if "!REMOTE_HOST!"=="your-server-ip" (
    echo [ERROR] 请先配置 scripts\deploy-config.txt 文件
    pause
    exit /b 1
)

echo [INFO] 配置信息:
echo   服务器: !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_PORT!
echo   后端路径: !REMOTE_BACKEND_PATH!
echo   服务名称: !REMOTE_BACKEND_SERVICE!
echo.
echo ⚠️  请确认以上配置是否正确！
echo.
set /p CONFIRM="确认部署后端？(Y/N): "
if /i not "!CONFIRM!"=="Y" (
    echo 部署已取消
    pause
    exit /b 0
)
echo.

REM 检查本地jar文件
echo [1/4] 检查本地文件...
if not exist "!LOCAL_BACKEND_JAR!" (
    echo [ERROR] 后端jar文件不存在: !LOCAL_BACKEND_JAR!
    echo.
    echo 请先编译后端: 双击 scripts\build-backend.bat
    pause
    exit /b 1
)
echo [OK] 找到jar文件: !LOCAL_BACKEND_JAR!
echo.

REM 检查SCP是否可用
where scp >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未找到 scp 命令
    echo 请安装 OpenSSH 或使用手动上传方式
    pause
    exit /b 1
)

REM 检查并创建远程目录
echo [2/4] 检查远程目录...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "mkdir -p !REMOTE_BACKEND_PATH!" >nul 2>&1
if errorlevel 1 (
    echo ⚠️  警告: 无法创建远程目录（可能已存在或需要手动创建）
)
echo [OK] 远程目录检查完成
echo.

REM 上传jar文件
echo [3/4] 上传后端jar包...
scp -P !REMOTE_PORT! "!LOCAL_BACKEND_JAR!" !REMOTE_USER!@!REMOTE_HOST!:!REMOTE_BACKEND_PATH!/iot-platform.jar
if errorlevel 1 (
    echo [ERROR] 上传失败
    echo.
    echo 可能的原因：
    echo   1. SSH连接失败（检查IP、端口、用户名）
    echo   2. 远程路径不存在（需要手动创建：!REMOTE_BACKEND_PATH!）
    echo   3. 权限不足（检查SSH密钥或密码）
    echo   4. 网络连接问题
    echo.
    pause
    exit /b 1
)
echo [OK] jar包已上传到: !REMOTE_BACKEND_PATH!/iot-platform.jar
echo.

REM 重启后端服务
echo [4/4] 重启后端服务...
ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST! "systemctl restart !REMOTE_BACKEND_SERVICE!"
if errorlevel 1 (
    echo [ERROR] 重启服务失败
    echo.
    echo 请手动执行：
    echo   ssh -p !REMOTE_PORT! !REMOTE_USER!@!REMOTE_HOST!
    echo   systemctl restart !REMOTE_BACKEND_SERVICE!
    echo.
    pause
    exit /b 1
)
echo [OK] 后端服务已重启
echo.

echo ========================================
echo   ✅ 后端部署完成
echo ========================================
echo.
echo 部署信息:
echo   - jar包路径: !REMOTE_BACKEND_PATH!/iot-platform.jar
echo   - 服务名称: !REMOTE_BACKEND_SERVICE!
echo.
pause
endlocal
