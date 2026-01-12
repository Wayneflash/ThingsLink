@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM Get script directory
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM Change to project root
cd /d "!SCRIPT_DIR!\.."

echo ========================================
echo   Git Push - Commit and Push to Remote
echo ========================================
echo.
echo Project: %CD%
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

REM Show current status
echo ========================================
echo   Current Changes:
echo ========================================
git status --short
echo.

REM Check if there are changes
for /f %%i in ('git status --porcelain 2^>nul ^| find /c /v ""') do set CHANGE_COUNT=%%i

if "%CHANGE_COUNT%"=="0" (
    echo [INFO] No changes to commit
    echo.
    echo Push existing commits to remote?
    set /p PUSH_ONLY="Enter Y to push, other key to exit: "
    if /i not "!PUSH_ONLY!"=="Y" (
        echo Cancelled
        pause
        exit /b 0
    )
    goto :do_push
)

echo ========================================
echo   This will:
echo ========================================
echo   1. Add all changes
echo   2. Commit with message
echo   3. Force push to remote
echo.

REM First confirmation
set /p CONFIRM1="Confirm commit and push? (Y/N): "
if /i not "!CONFIRM1!"=="Y" (
    echo.
    echo Cancelled
    pause
    exit /b 0
)

echo.
echo ========================================
echo   Committing...
echo ========================================
echo.

REM Add all changes
echo [1/4] Adding all changes...
git add .
if errorlevel 1 (
    echo [ERROR] Add failed
    pause
    exit /b 1
)
echo [OK] All changes added

REM Enter commit message
echo.
echo [2/4] Enter commit message...
echo.
echo Enter commit message (press Enter for default timestamp):
set /p COMMIT_MSG_INPUT="Message: "

if "!COMMIT_MSG_INPUT!"=="" (
    for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
    set COMMIT_MSG=Auto commit: !dt:~0,4!-!dt:~4,2!-!dt:~6,2! !dt:~8,2!:!dt:~10,2!
    echo [INFO] Using default message: !COMMIT_MSG!
) else (
    set COMMIT_MSG=!COMMIT_MSG_INPUT!
)

REM Confirm message
echo.
echo Commit message: !COMMIT_MSG!
set /p CONFIRM_MSG="Confirm message? (Y/N): "
if /i not "!CONFIRM_MSG!"=="Y" (
    echo.
    echo Cancelled
    pause
    exit /b 0
)

REM Commit
echo.
echo [3/4] Committing...
git commit -m "!COMMIT_MSG!"
if errorlevel 1 (
    echo [WARNING] Commit may have failed or no changes to commit
)
echo [OK] Commit done

:do_push
REM Push
echo.
echo [4/4] Pushing to remote...
echo.
echo Target: origin/%CURRENT_BRANCH%
echo.
set /p CONFIRM_PUSH="Confirm push? (Y/N): "
if /i not "!CONFIRM_PUSH!"=="Y" (
    echo.
    echo Push cancelled
    pause
    exit /b 0
)

echo.
echo Executing: git push origin %CURRENT_BRANCH% --force
echo.
git push origin %CURRENT_BRANCH% --force

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Push failed with error code: %ERRORLEVEL%
    echo.
    echo Possible reasons:
    echo   1. Network issue
    echo   2. No permission
    echo   3. Wrong remote URL
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   [OK] Push successful!
echo ========================================
echo.
echo Pushed to: origin/%CURRENT_BRANCH%
echo.
pause
endlocal
