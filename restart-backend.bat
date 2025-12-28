@echo off
chcp 65001 >nul
echo ========================================
echo 正在重启后端服务...
echo ========================================

echo.
echo [1/3] 停止当前后端进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a
)

echo.
echo [2/3] 等待端口释放...
timeout /t 3 /nobreak >nul

echo.
echo [3/3] 启动后端服务...
cd /d E:\IOT\backend
start "IOT Backend" cmd /k "set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 && mvn spring-boot:run"

echo.
echo ========================================
echo 后端服务启动中，请等待启动完成...
echo 启动成功后会显示: IoT Platform Started Successfully!
echo ========================================
pause
