@echo off
echo ========================================
echo   Git Push Script
echo ========================================
echo.

REM Check if current directory is a Git repository
if exist ".git" (
    echo [INFO] Found Git repository in: %CD%
    goto :found_git
)

REM Check subdirectories for Git repository (ThingsLink, etc.)
echo [INFO] Checking subdirectories for Git repository...
for /d %%d in (*) do (
    if exist "%%d\.git" (
        echo [INFO] Found Git repository in: %CD%\%%d
        cd /d "%CD%\%%d"
        goto :found_git
    )
)

REM If not found, ask user for project path
echo [INFO] Git repository not found in current directory or subdirectories
echo Current directory: %CD%
echo.
set /p project_path=Enter your project path (or press Enter to exit): 

if "%project_path%"=="" (
    echo [ERROR] Please navigate to your project directory first
    echo Example: cd /d "E:\IOT"
    pause
    exit /b 1
)

REM Check if the entered path is valid
if not exist "%project_path%\.git" (
    echo [ERROR] Not a valid Git repository: %project_path%
    echo Please make sure the path contains a .git folder
    pause
    exit /b 1
)

cd /d "%project_path%"
echo [INFO] Changed to: %CD%

:found_git
echo.
set /p commit_msg=Enter commit message (press Enter for default): 

if "%commit_msg%"=="" (
    set commit_msg=Update code
)

echo.
echo [1/3] Adding changes...
git add .

echo [2/3] Committing...
git commit -m "%commit_msg%"

echo [3/3] Pushing to GitHub...
git push origin main

if errorlevel 1 (
    echo.
    echo [WARNING] Push failed, trying force push...
    git push origin main --force
)

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo Repository: https://github.com/Wayneflash/ThingsLink
echo.
pause
