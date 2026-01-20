@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory (auto adapt different paths)
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Start UniApp Mobile Application
echo ========================================
echo.
echo Project Path: %CD%
echo Note: Ensure backend service and Docker are already started
echo.

REM Navigate to UniApp directory
cd iot-mobile-app

REM Start UniApp development server
echo Starting UniApp development server...
echo.
npm run dev:h5

pause
endlocal
