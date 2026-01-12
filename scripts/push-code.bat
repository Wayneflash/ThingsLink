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
    set /p PUSH_ONLY="Push existing commits? (Y/N): "
    if /i not "!PUSH_ONLY!"=="Y" (
        echo Cancelled
        pause
        exit /b 0
    )
    goto :do_push
)

REM Enter commit message FIRST
echo ========================================
echo   Enter Commit Message
echo ========================================
echo.
echo (Press Enter for auto timestamp)
echo.
set /p COMMIT_MSG_INPUT="Message: "

if "!COMMIT_MSG_INPUT!"=="" (
    for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
    set COMMIT_MSG=Auto commit: !dt:~0,4!-!dt:~4,2!-!dt:~6,2! !dt:~8,2!:!dt:~10,2!
    echo.
    echo [INFO] Using: !COMMIT_MSG!
) else (
    set COMMIT_MSG=!COMMIT_MSG_INPUT!
)

echo.
echo ========================================
echo   Confirm Operation
echo ========================================
echo.
echo   Message: !COMMIT_MSG!
echo   Branch:  %CURRENT_BRANCH%
echo   Action:  Add all + Commit + Force Push
echo.
set /p CONFIRM="Confirm? (Y/N): "
if /i not "!CONFIRM!"=="Y" (
    echo.
    echo Cancelled
    pause
    exit /b 0
)

echo.
echo ========================================
echo   Executing...
echo ========================================
echo.

REM Add all changes
echo [1/3] Adding all changes...
git add .
if errorlevel 1 (
    echo [ERROR] Add failed
    pause
    exit /b 1
)
echo [OK] Done

REM Commit
echo [2/3] Committing...
git commit -m "!COMMIT_MSG!"
if errorlevel 1 (
    echo [WARNING] Commit may have failed
)
echo [OK] Done

:do_push
REM Push
echo [3/3] Pushing to origin/%CURRENT_BRANCH%...
git push origin %CURRENT_BRANCH% --force

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Push failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo   SUCCESS!
echo ========================================
echo.
echo Pushed to: origin/%CURRENT_BRANCH%
echo.
pause
endlocal
