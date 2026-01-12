@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   WARNING: Pull Remote Code
echo ========================================
echo.
echo Project: %CD%
echo.
echo ========================================
echo   WARNING WARNING WARNING
echo ========================================
echo.
echo This will:
echo   1. DISCARD all local uncommitted changes
echo   2. OVERWRITE local with remote code
echo   3. Local changes will be PERMANENTLY LOST!
echo.

REM Check if Git is installed
where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git not installed
    pause
    exit /b 1
)

REM Check if current directory is a Git repository
if not exist ".git" (
    echo [ERROR] Git repository not found
    pause
    exit /b 1
)

REM Get current branch
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if "%CURRENT_BRANCH%"=="" set CURRENT_BRANCH=main
echo [INFO] Branch: %CURRENT_BRANCH%
echo.

REM Show current uncommitted changes
echo ========================================
echo   Uncommitted Changes (will be LOST):
echo ========================================
git status --short
echo.

REM Check if there are uncommitted changes
git diff --quiet >nul 2>&1
set HAS_CHANGES=0
if errorlevel 1 set HAS_CHANGES=1

git diff --cached --quiet >nul 2>&1
if errorlevel 1 set HAS_CHANGES=1

if %HAS_CHANGES%==1 (
    echo [WARNING] You have uncommitted changes!
    echo.
    echo If you continue, these changes will be PERMANENTLY LOST!
    echo.
) else (
    echo [OK] No uncommitted changes
    echo.
)

REM First confirmation
echo ========================================
set /p CONFIRM1="Confirm pull remote code? (Y/N): "
if /i not "!CONFIRM1!"=="Y" (
    echo.
    echo Cancelled
    pause
    exit /b 0
)

REM Second confirmation (if has uncommitted changes)
if %HAS_CHANGES%==1 (
    echo.
    echo [WARNING] Confirm again: Local changes will be PERMANENTLY LOST!
    set /p CONFIRM2="Type YES to discard all local changes: "
    if /i not "!CONFIRM2!"=="YES" (
        echo.
        echo Cancelled
        pause
        exit /b 0
    )
)

echo.
echo ========================================
echo   Pulling code...
echo ========================================
echo.

REM Discard all local changes
echo [1/4] Discarding local changes...
git reset --hard HEAD >nul 2>&1
git clean -fd >nul 2>&1
echo [OK] Local changes discarded

REM Fetch latest code
echo.
echo [2/4] Fetching remote code...
git fetch origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [WARNING] Fetch failed, trying fetch all...
    git fetch origin
    if errorlevel 1 (
        echo [ERROR] Fetch failed
        pause
        exit /b 1
    )
)
echo [OK] Fetch successful

REM Reset to remote branch
echo.
echo [3/4] Syncing to remote branch...
git reset --hard origin/%CURRENT_BRANCH% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Reset failed
    pause
    exit /b 1
)
echo [OK] Sync successful

REM Clean untracked files
echo.
echo [4/4] Cleaning files...
git clean -fd >nul 2>&1

REM Show latest commit
echo.
echo [INFO] Latest commit:
git log -1 --oneline

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo Synced with remote: origin/%CURRENT_BRANCH%
echo.
pause
endlocal
