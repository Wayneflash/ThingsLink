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

REM Check if iot-mobile-app directory exists
if not exist "iot-mobile-app" (
    echo [ERROR] iot-mobile-app directory not found!
    echo Current directory: %CD%
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

REM Navigate to UniApp directory
cd iot-mobile-app
echo [INFO] Changed to: %CD%
echo.

REM Check if package.json exists
if not exist "package.json" (
    echo [ERROR] package.json not found in iot-mobile-app directory!
    echo Current directory: %CD%
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

REM Check if npm is available
where npm >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm command not found!
    echo Please install Node.js and npm first.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

REM Check if node_modules exists
if not exist "node_modules" (
    echo [ERROR] node_modules directory not found!
    echo Please run: npm install
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

REM Start UniApp development server
echo [INFO] Starting UniApp development server...
echo.

REM Set environment variables directly (Windows supports this natively)
REM This avoids the cross-env dependency issue
set NODE_ENV=development
set UNI_PLATFORM=h5
echo [INFO] Environment variables set: NODE_ENV=%NODE_ENV%, UNI_PLATFORM=%UNI_PLATFORM%
echo.

REM Add node_modules/.bin to PATH so we can call commands directly
set "PATH=%CD%\node_modules\.bin;%PATH%"

REM Try multiple ways to run vue-cli-service (bypassing cross-env)
echo [INFO] Looking for vue-cli-service...
echo.

REM Check which method to use
REM Priority: JS file > npm run > CMD wrapper (CMD wrapper has issues with uni-serve)
set "USE_METHOD="
if exist "node_modules\@vue\cli-service\bin\vue-cli-service.js" (
    set "USE_METHOD=JS"
    echo [INFO] Found: vue-cli-service.js (recommended)
) else if exist "node_modules\.bin\vue-cli-service.cmd" (
    set "USE_METHOD=CMD"
    echo [INFO] Found: vue-cli-service.cmd (may have issues)
) else if exist "node_modules\.bin\vue-cli-service" (
    set "USE_METHOD=BIN"
    echo [INFO] Found: vue-cli-service
) else (
    set "USE_METHOD=NPM"
    echo [WARNING] vue-cli-service not found in expected locations
    echo [INFO] Will try npm run dev:h5
)

echo.
echo [INFO] Starting development server...
echo [INFO] Server will be available at http://localhost:8081/
echo [INFO] Press Ctrl+C to stop the server
echo.
echo ========================================
echo.

REM Execute based on method found
REM JS method is most reliable (tested and working)
if "%USE_METHOD%"=="JS" (
    node node_modules\@vue\cli-service\bin\vue-cli-service.js uni-serve
    goto :server_stopped
)

if "%USE_METHOD%"=="CMD" (
    REM CMD wrapper may not support uni-serve, so try direct node call instead
    echo [WARNING] vue-cli-service.cmd may not support uni-serve command
    echo [INFO] Trying direct node call instead...
    if exist "node_modules\@vue\cli-service\bin\vue-cli-service.js" (
        node node_modules\@vue\cli-service\bin\vue-cli-service.js uni-serve
        goto :server_stopped
    )
    REM If JS file doesn't exist, try CMD anyway
    vue-cli-service uni-serve
    goto :server_stopped
)

if "%USE_METHOD%"=="BIN" (
    vue-cli-service uni-serve
    goto :server_stopped
)

REM Last resort: try npm run
if "%USE_METHOD%"=="NPM" (
    npm run dev:h5
    goto :server_stopped
)

REM If we get here, something went wrong
echo [ERROR] Could not determine how to start vue-cli-service!
goto :error_exit

:error_exit
echo.
echo [ERROR] Development server failed to start!
echo Please check the error messages above.
echo.
echo Press any key to exit...
pause >nul
exit /b 1

:server_stopped
REM Note: If we reach here, the server has stopped
REM This is normal - the server runs until Ctrl+C is pressed
echo.
echo [INFO] Development server stopped.
echo.
echo Press any key to exit...
pause >nul
endlocal
