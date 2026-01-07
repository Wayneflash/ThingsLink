@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
echo ========================================
echo   Git Pull Script
echo ========================================
echo.

REM Check if Git is installed
where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git not installed
    pause
    exit /b 1
)

REM Check if current directory is a Git repository
if exist ".git" (
    echo [INFO] Found Git repository in: %CD%
    goto :start_pull
)

REM Check subdirectories
for /d %%d in (*) do (
    if exist "%%d\.git" (
        echo [INFO] Found Git repository in: %CD%\%%d
        cd /d "%CD%\%%d"
        goto :start_pull
    )
)

echo [ERROR] Git repository not found
pause
exit /b 1

:start_pull
REM Get current branch
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if "%CURRENT_BRANCH%"=="" set CURRENT_BRANCH=main
echo [INFO] Current branch: %CURRENT_BRANCH%

echo.
echo [1/3] Fetching latest code...
git fetch origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [ERROR] Fetch failed
    pause
    exit /b 1
)

echo.
echo [2/3] Pulling remote code...
git pull origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [WARNING] Pull may have conflicts, please check
)

echo.
echo [3/3] Cleaning compiled files...
if exist "backend\target\" (
    git rm -r --cached backend/target/ >nul 2>&1
)
if exist "frontend\node_modules\.vite\" (
    git rm -r --cached frontend/node_modules/.vite/ >nul 2>&1
)

echo.
echo [INFO] Current status:
git status --short

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
pause
