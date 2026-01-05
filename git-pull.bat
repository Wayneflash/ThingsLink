@echo off
echo ========================================
echo   Git Pull Script
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
echo [1/2] Pulling latest code...
git pull origin main

if errorlevel 1 (
    echo.
    echo [WARNING] Pull failed, trying force update...
    git fetch origin main
    git reset --hard origin/main
)

echo.
echo [2/2] Showing status...
git status

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
pause
