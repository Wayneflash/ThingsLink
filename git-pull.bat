@echo off
echo ========================================
echo   Git Pull Script (Pull Code+Backup)
echo ========================================
echo.

REM Check if Git is installed
where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git not installed, please install Git first
    echo Download: https://git-scm.com/download/win
    pause
    exit /b 1
)

REM Check if current directory is a Git repository
if exist ".git" (
    echo [INFO] Found Git repository in: %CD%
    goto :found_git
)

REM Check subdirectories for Git repository
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
REM Get current branch name
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if "%CURRENT_BRANCH%"=="" (
    REM Try to get branch from git status
    for /f "tokens=*" %%b in ('git rev-parse --abbrev-ref HEAD 2^>nul') do set CURRENT_BRANCH=%%b
)
if "%CURRENT_BRANCH%"=="" (
    echo [WARNING] Cannot detect current branch, using 'main' as default
    set CURRENT_BRANCH=main
) else (
    echo [INFO] Current branch: %CURRENT_BRANCH%
)

echo.
echo [1/2] Pulling latest code from origin/%CURRENT_BRANCH%...
git pull origin %CURRENT_BRANCH%

if errorlevel 1 (
    echo.
    echo [WARNING] Pull failed, trying fetch and merge...
    git fetch origin %CURRENT_BRANCH%
    if errorlevel 1 (
        echo [ERROR] Fetch failed. Please check your network connection and remote repository.
        pause
        exit /b 1
    )
    git merge origin/%CURRENT_BRANCH%
    if errorlevel 1 (
        echo [WARNING] Merge conflicts detected. Please resolve manually.
    )
)

echo.
echo [2/2] Showing status...
git status

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo Latest code pulled (including database backup file)
echo To start services, run: start-all-services.bat
echo.
pause
