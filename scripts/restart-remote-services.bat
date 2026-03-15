@echo off
chcp 65001 >nul
echo ========================================
echo   重启远程服务器 Docker 容器
echo ========================================
echo.
echo 服务器: 117.72.162.225
echo.
echo 正在连接服务器并重启容器...
echo.

ssh root@117.72.162.225 "cd /opt/iot-platform && docker-compose restart && docker-compose ps"

echo.
echo ========================================
echo   服务重启完成！
echo ========================================
echo.
echo 请保持SSH隧道窗口打开，然后重启本地后端应用
echo.
pause
