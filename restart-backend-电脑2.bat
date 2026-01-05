@echo off
chcp 65001 >nul
echo ========================================
echo 正在重启后端服务...
echo ========================================

REM 设置Java环境变量
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [1/2] 停止占用 8080 端口的进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a
)
timeout /t 2 /nobreak >nul

echo.
echo [2/2] 启动后端服务...
cd /d D:\AICoding\IOT\backend

if not exist "target\iot-platform.jar" (
    echo 错误: 找不到 target\iot-platform.jar
    echo 请先编译: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo 后端将在新窗口启动，请查看新窗口了解启动状态
start "IOT Backend" cmd /k "java -jar target\iot-platform.jar"

echo.
echo ========================================
echo 启动完成！
echo ========================================
echo.
echo 服务地址: http://localhost:8080
echo 请在新窗口查看启动日志
echo 等待看到 'IoT Platform Started Successfully!' 即启动成功
echo.
pause
