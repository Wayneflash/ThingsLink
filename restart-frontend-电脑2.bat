@echo off
chcp 65001 >nul
echo ========================================
echo            IOT 前端服务启动脚本
echo ========================================
echo.

REM 设置Node.js环境变量（根据实际安装路径调整）
set NODE_PATH=D:\Node\1
if exist "%NODE_PATH%\node.exe" (
    set PATH=%NODE_PATH%;%PATH%
    echo [✓] Node.js路径: %NODE_PATH%
) else (
    echo [!] Node.js未在默认路径找到，使用系统PATH
)

REM 检查前端目录
set FRONTEND_DIR=D:\AICoding\IOT\frontend
if not exist "%FRONTEND_DIR%\package.json" (
    echo [错误] 找不到前端目录: %FRONTEND_DIR%
    pause
    exit /b 1
)
echo [✓] 前端目录: %FRONTEND_DIR%

echo.
echo [1/4] 停止当前前端进程...
set PORT=5173
set FOUND=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%PORT% ^| findstr LISTENING') do (
    echo 发现进程 PID: %%a，正在停止...
    taskkill /F /PID %%a 2>nul
    set FOUND=1
)
if %FOUND%==0 (
    echo 未发现占用端口 %PORT% 的进程
)

echo.
echo [2/4] 等待端口释放...
timeout /t 2 /nobreak >nul

echo.
echo [3/4] 检查依赖...
cd /d "%FRONTEND_DIR%"
if not exist "node_modules" (
    echo 未找到 node_modules，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
    echo [✓] 依赖安装完成
) else (
    echo [✓] node_modules 已存在
)

echo.
echo [4/4] 启动前端服务...
echo 正在启动 Vite 开发服务器...
start "IOT Frontend" cmd /k "npm run dev"

echo.
echo ========================================
echo           启动信息
echo ========================================
echo 前端服务启动中，请等待启动完成...
echo 启动成功后可访问: http://localhost:5173
echo.
echo 如果启动失败，请检查：
echo 1. Node.js 是否正确安装 (node --version)
echo 2. 端口 5173 是否被其他程序占用
echo 3. 依赖是否完整 (npm install)
echo ========================================
echo.
echo 按任意键关闭此窗口...
pause >nul
