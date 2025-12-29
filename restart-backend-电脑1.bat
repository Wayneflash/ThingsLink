@echo off
chcp 65001 >nul
echo ========================================
echo 正在重启后端服务...
echo ========================================

echo.
echo [1/5] 检查并启动 Docker Desktop...
tasklist /FI "IMAGENAME eq Docker Desktop.exe" 2>NUL | find /I /N "Docker Desktop.exe">NUL
if "%ERRORLEVEL%"=="1" (
    echo Docker Desktop 未运行，正在启动...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo 等待 Docker Desktop 启动 (30秒)...
    timeout /t 30 /nobreak >nul
) else (
    echo Docker Desktop 已运行
)

echo.
echo [2/5] 启动 Docker 容器服务...
cd /d E:\IOT
docker-compose up -d
echo 等待服务就绪 (10秒)...
timeout /t 10 /nobreak >nul

echo.
echo [3/5] 停止当前后端进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a
)

echo.
echo [4/5] 等待端口释放...
timeout /t 3 /nobreak >nul

echo.
echo [5/5] 启动后端服务...
cd /d E:\IOT\backend
start "IOT Backend" cmd /k "set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 && mvn spring-boot:run"

echo.
echo ========================================
echo 后端服务启动中，请等待启动完成...
echo 启动成功后会显示: IoT Platform Started Successfully!
echo ========================================
pause
