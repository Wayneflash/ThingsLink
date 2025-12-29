@echo off
chcp 65001 >nul
echo ========================================
echo 正在重启前端服务...
echo ========================================

REM 设置Node.js环境变量
set PATH=D:\Node\1;D:\Node\1\node_modules;%PATH%

echo.
echo [1/3] 停止当前前端进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a
)

echo.
echo [2/3] 等待端口释放...
timeout /t 3 /nobreak >nul

echo.
echo [3/3] 启动前端服务...
cd /d D:\AICoding\IOT\frontend
start "IOT Frontend" cmd /k "npm run dev"

echo.
echo ========================================
echo 前端服务启动中，请等待启动完成...
echo 启动成功后可访问: http://localhost:5173
echo ========================================
pause
