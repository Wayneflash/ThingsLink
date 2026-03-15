@echo off
chcp 65001 >nul

echo ========================================
echo   SSH Tunnel - 117.72.162.225
echo ========================================
echo.
echo Ports: 23307=MySQL  26379=Redis  21183=MQTT
echo.
echo If you use SSH key: no input needed, just keep this window OPEN.
echo If password login: enter password when prompted.
echo ========================================
echo.

ssh -L 23306:localhost:13306 -L 23307:localhost:13307 -L 26379:localhost:16379 -L 21183:localhost:11883 -L 28083:localhost:18083 -L 29083:localhost:28083 root@117.72.162.225 -N

pause
