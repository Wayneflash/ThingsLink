@echo off
chcp 65001 >nul
echo Checking if SSH tunnel ports are listening...
echo.
netstat -an | findstr "23307 26379 21183" | findstr "LISTENING"
if errorlevel 1 (
    echo [NOT FOUND] No process listening on 23307/26379/21183.
    echo Run scripts\start-ssh-tunnel.bat and keep that window open.
) else (
    echo [OK] Tunnel is up. Backend can use dev-remote.
)
echo.
pause
