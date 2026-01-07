@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
echo ========================================
echo   Git Pull Script (Smart Sync - Ensure Code Consistency)
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
echo [1/4] Pulling latest code...
git fetch origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [ERROR] Fetch failed. Please check your network connection and remote repository.
    pause
    exit /b 1
)

REM Check if there are local changes
git status --porcelain | findstr /R "^[MADRC]" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Detected uncommitted local changes
    echo [INFO] Modified files:
    git status --short
    echo.
    set /p stash_confirm=Stash local changes? (y/n, recommended: y): 
    if /i "!stash_confirm!"=="y" (
        echo [INFO] Stashing local changes...
        git stash push -m "Auto stash before pull - %date% %time%"
        set STASHED=1
    ) else (
        echo [WARNING] Local changes not stashed, may cause conflicts
        set STASHED=0
    )
) else (
    set STASHED=0
)

echo.
echo [2/4] Merging remote code...
git pull origin %CURRENT_BRANCH%
set GIT_PULL_ERROR=%errorlevel%

if %GIT_PULL_ERROR% neq 0 (
    echo.
    echo [WARNING] Pull failed, trying merge...
    git merge origin/%CURRENT_BRANCH%
    if errorlevel 1 (
        echo [ERROR] Merge conflicts detected! Please resolve manually
        echo.
        echo Conflicted files:
        git status --short | findstr /R "^UU"
        echo.
        echo After resolving conflicts, run:
        echo   git add .
        echo   git commit -m "Resolve merge conflicts"
        echo   git push origin %CURRENT_BRANCH%
        pause
        exit /b 1
    )
)

REM Restore stashed changes if any
if "%STASHED%"=="1" (
    echo.
    echo [3/4] Restoring local changes...
    git stash pop
    if errorlevel 1 (
        echo [WARNING] Conflicts may occur when restoring local changes, please check
    ) else (
        echo [OK] Local changes restored
    )
) else (
    echo [3/4] Skipping restore (no local changes)
)

echo.
echo [4/4] Cleaning compiled files (ensuring code consistency)...
REM Remove compiled files that should not be in Git
if exist "backend\target\" (
    echo [INFO] Cleaning backend compiled files...
    REM Keep the directory but remove tracked files
    git rm -r --cached backend/target/ 2>nul
)
if exist "frontend\node_modules\.vite\" (
    echo [INFO] Cleaning frontend compiled cache...
    git rm -r --cached frontend/node_modules/.vite/ 2>nul
)

REM Check for any compiled files that should be ignored
git status --porcelain | findstr /C:"target/" /C:"node_modules/.vite/" /C:".class" /C:".jar" >nul 2>&1
if not errorlevel 1 (
    echo [INFO] Found compiled files, removed from Git tracking
)

echo.
echo [INFO] Current code status:
git status --short

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo [Summary]
echo - Latest code pulled
echo - Compiled files cleaned
echo - Code synchronized with remote repository
echo.
echo [Next Steps]
echo - Run start-all-services.bat to start services
echo - If conflicts exist, resolve them manually and commit
echo.
pause
