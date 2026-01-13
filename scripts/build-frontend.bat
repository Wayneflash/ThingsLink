@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory (auto adapt different paths)
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root directory
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Frontend Build
echo ========================================
echo.
echo Project: %CD%
echo.

REM Check if Node.js is installed
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not installed
    echo Please install Node.js: https://nodejs.org/
    pause
    exit /b 1
)

REM Display Node.js version
for /f "tokens=*" %%v in ('node -v') do echo [INFO] Node.js version: %%v

REM Check if frontend directory exists
if not exist "frontend" (
    echo [ERROR] frontend directory not found
    pause
    exit /b 1
)

REM Change to frontend directory
cd frontend

REM Check if package.json exists
if not exist "package.json" (
    echo [ERROR] package.json not found
    pause
    exit /b 1
)

REM Check if node_modules exists
if not exist "node_modules" (
    echo [INFO] node_modules not found, installing dependencies...
    echo.
    npm install
    if errorlevel 1 (
        echo [ERROR] npm install failed
        pause
        exit /b 1
    )
    echo [OK] Dependencies installed
    echo.
)

REM Execute build
echo [INFO] Building frontend...
echo.
npm run build

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Build Success!
echo ========================================
echo.
echo Output: frontend\dist\
echo.

REM Check dist directory
if exist "dist\index.html" (
    echo [OK] dist\index.html exists
) else (
    echo [WARNING] dist\index.html not found
)

echo.
echo Next steps:
echo   - Local test: npm run dev
echo   - Deploy: scripts\deploy-frontend.bat
echo.
pause
endlocal
