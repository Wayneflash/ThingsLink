@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录（scripts目录的上一级）
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Git Pull Script (Remote Overwrites Local)
echo ========================================
echo.
echo 项目路径: !SCRIPT_DIR!
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
    echo 当前目录: !SCRIPT_DIR!
    echo 请确保脚本在项目根目录下
    pause
    exit /b 1
)

echo [INFO] Found Git repository in: !SCRIPT_DIR!

REM Get current branch
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if "%CURRENT_BRANCH%"=="" set CURRENT_BRANCH=main
echo [INFO] Current branch: %CURRENT_BRANCH%

REM Discard all local changes first
echo.
echo [0/4] Discarding all local changes...
git reset --hard HEAD >nul 2>&1
git clean -fd -e pull-code.bat -e push-code.bat >nul 2>&1
echo [OK] Local changes discarded

REM Fetch latest code
echo.
echo [1/4] Fetching latest code...
git fetch origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [WARNING] Fetch with branch failed, trying fetch all...
    git fetch origin
    if errorlevel 1 (
        echo [ERROR] Fetch failed
        pause
        exit /b 1
    )
)
echo [OK] Fetch successful

REM Reset to remote branch (overwrite local completely)
echo.
echo [2/4] Resetting to remote branch (will overwrite local)...
git reset --hard origin/%CURRENT_BRANCH% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Reset failed
    pause
    exit /b 1
)
echo [OK] Reset to remote branch successful

REM Clean untracked files
echo.
echo [3/4] Cleaning untracked files...
git clean -fd -e pull-code.bat -e push-code.bat >nul 2>&1

REM Show status
echo.
echo [4/4] Checking status...
git status --short

REM Show latest commit
echo.
echo [INFO] Latest commit:
git log -1 --oneline

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo [Summary]
echo - All local changes have been discarded
echo - Code synchronized with remote: origin/%CURRENT_BRANCH%
echo - Local has been overwritten with remote version
echo.
pause
endlocal
