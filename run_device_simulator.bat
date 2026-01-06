@echo off
chcp 65001 >nul
cd /d %~dp0
echo ========================================
echo   IoT设备模拟器启动
echo ========================================
echo.
py device_simulator.py
pause

