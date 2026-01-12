@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到脚本所在目录
cd /d "!SCRIPT_DIR!"

echo ========================================
echo   Git Push Script (Auto Commit and Push)
echo ========================================
echo.
echo 项目路径: !SCRIPT_DIR!
echo.
echo Function:
echo   - Auto add all local changes
echo   - Auto commit (with custom message or timestamp)
echo   - Auto push to GitHub (overwrite remote)
echo   Just double-click to run, no manual git commands needed!
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
    echo Current directory: !SCRIPT_DIR!
    echo Please ensure script is in project root directory
    pause
    exit /b 1
)

echo [INFO] Found Git repository in: !SCRIPT_DIR!

REM Get current branch
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if "%CURRENT_BRANCH%"=="" set CURRENT_BRANCH=main
echo [INFO] Current branch: %CURRENT_BRANCH%

REM Check status
echo.
echo [1/5] Checking status...
git status --short
if errorlevel 1 (
    echo [ERROR] Failed to check status
    pause
    exit /b 1
)

REM Add all changes
echo.
echo [2/5] Adding all changes...
git add .
if errorlevel 1 (
    echo [ERROR] Failed to add files
    pause
    exit /b 1
)
echo [OK] All changes added

REM Check if there are changes to commit
git diff --cached --quiet >nul 2>&1
if not errorlevel 1 (
    echo [INFO] No changes to commit
    goto :push_only
)

REM Commit changes
echo.
echo [3/5] Committing changes...
echo.
echo Enter commit message (press Enter for default timestamp):
set /p COMMIT_MSG_INPUT="Commit message: "

REM 如果用户没有输入，使用默认时间戳
if "!COMMIT_MSG_INPUT!"=="" (
    REM 生成时间戳格式的提交消息
    for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set DATE_PART=%%c-%%a-%%b
    for /f "tokens=1-2 delims=: " %%a in ('time /t') do set TIME_PART=%%a:%%b
    set COMMIT_MSG=Auto commit: !DATE_PART! !TIME_PART!
    echo [INFO] Using default commit message: !COMMIT_MSG!
) else (
    set COMMIT_MSG=!COMMIT_MSG_INPUT!
    echo [INFO] Using custom commit message: !COMMIT_MSG!
)

echo.
git commit -m "!COMMIT_MSG!"
if errorlevel 1 (
    echo [ERROR] Failed to commit
    echo Possible reason: No changes to commit
    pause
    exit /b 1
)
echo [OK] Changes committed: !COMMIT_MSG!

:push_only
REM Fetch remote first to check for updates
echo.
echo [4/5] Fetching remote updates...
git fetch origin %CURRENT_BRANCH% >nul 2>&1

REM Force push to overwrite remote (local takes priority)
echo.
echo [5/5] Pushing to remote (will overwrite remote with local)...
git push origin %CURRENT_BRANCH% --force
if errorlevel 1 (
    echo [ERROR] Push failed
    echo.
    echo Possible reasons:
    echo   1. Network connection problem
    echo   2. No permission to push
    echo   3. Remote repository URL is incorrect
    echo.
    pause
    exit /b 1
)

echo [OK] Push successful!

REM Show final status
echo.
echo [INFO] Final status:
git status --short

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo [Summary]
echo - All local changes have been committed
echo - Code pushed to remote: origin/%CURRENT_BRANCH%
echo - Remote has been overwritten with local version
echo.
echo Tips:
echo   - You can enter a custom commit message when prompted
echo   - Press Enter to use default timestamp message
echo   - The script will automatically:
echo     1. Check status
echo     2. Add all changes
echo     3. Commit changes (with your message or timestamp)
echo     4. Force push to GitHub (overwrite remote)
echo.
pause
endlocal
