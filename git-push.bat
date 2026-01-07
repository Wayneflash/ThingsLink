@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
echo ========================================
echo   Git Push Script (Smart Sync - Source Code Only)
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
set /p commit_msg=Enter commit message (press Enter for default): 

if "%commit_msg%"=="" (
    set commit_msg=Update code
)

REM Auto backup database (if Docker is running)
echo.
echo [0/5] Checking if database backup is needed...
REM Check if Docker is available
where docker >nul 2>&1
if errorlevel 1 (
    echo [INFO] Docker not installed or not in PATH, skipping database backup
    goto :skip_backup
)

docker ps --filter "name=iot-mysql" --format "{{.Names}}" 2>nul | findstr /i "iot-mysql" >nul 2>&1
if not errorlevel 1 (
    echo [INFO] MySQL container is running, backing up database...
    REM Use current directory and create backups folder
    set "BACKUP_DIR=backups"
    if not exist "%BACKUP_DIR%" (
        echo [INFO] Creating backup directory...
        mkdir "%BACKUP_DIR%" 2>nul
        if errorlevel 1 (
            echo [WARNING] Failed to create backup directory: %CD%\%BACKUP_DIR%
            echo [WARNING] Skipping database backup, continuing with code push
            goto :skip_backup
        ) else (
            echo [OK] Backup directory created: %CD%\%BACKUP_DIR%
        )
    ) else (
        echo [INFO] Backup directory already exists
    )
    set "BACKUP_FILE=%BACKUP_DIR%\iot_platform_latest.sql"
    
    echo [INFO] Backing up database...
    docker exec iot-mysql mysqldump -uroot -proot123456 iot_platform > "%BACKUP_FILE%" 2>&1
    if not errorlevel 1 (
        echo [OK] Database backup completed: %BACKUP_FILE%
    ) else (
        echo [WARNING] Database backup failed, continuing with code push
    )
) else (
    echo [INFO] MySQL container is not running, skipping database backup
)
:skip_backup

echo.
echo [1/5] Checking compiled files (ensuring they are not committed)...
REM Check if there are compiled files that should not be committed
git status --porcelain | findstr /C:"target/" /C:"node_modules/.vite/" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Found compiled files modified, removing from Git tracking...
    git rm -r --cached backend/target/ 2>nul
    git rm -r --cached frontend/node_modules/.vite/ 2>nul
    echo [OK] Compiled files removed from Git tracking
)

echo.
echo [2/5] Adding source code files (excluding compiled files)...
REM Add source files only (not compiled files)
REM First, ensure .gitignore is up to date
git add .gitignore 2>nul

REM Add source directories explicitly
git add frontend/src/ 2>nul
git add backend/src/ 2>nul
git add init.sql 2>nul
git add .cursorrules 2>nul
git add GIT_COMMIT_GUIDE.md 2>nul
git add GIT_SYNC_GUIDE.md 2>nul
git add *.bat 2>nul
git add *.md 2>nul
git add *.py 2>nul
git add *.sql 2>nul
git add prototypes/ 2>nul
git add backups/iot_platform_latest.sql 2>nul

REM Add any other important files
git add README.md 2>nul
git add package.json 2>nul
git add pom.xml 2>nul

REM Check if there are any untracked source files
echo [INFO] Checking for untracked source files...
git status --porcelain | findstr /R "^??" >nul 2>&1
if not errorlevel 1 (
    echo [INFO] Found untracked files, adding...
    REM Add untracked files but exclude compiled files
    for /f "tokens=2" %%f in ('git status --porcelain ^| findstr /R "^??"') do (
        echo %%f | findstr /C:"target/" /C:"node_modules/" /C:".class" /C:".jar" /C:".log" >nul 2>&1
        if errorlevel 1 (
            git add "%%f" 2>nul
        )
    )
)

set GIT_ADD_ERROR=%errorlevel%
if %GIT_ADD_ERROR% neq 0 (
    echo [WARNING] Some files may not have been added, but continuing...
)

echo.
echo [3/5] Checking files to be committed...
git status --short >nul 2>&1
if errorlevel 1 (
    echo [INFO] Checking change status...
    git status --short
)

REM Show what will be committed
echo.
echo [INFO] Files to be committed:
git status --short
echo.

REM Check if there are changes to commit
git diff --cached --quiet 2>nul
if not errorlevel 1 (
    git status --short | findstr /R "^[AMD]" >nul 2>&1
    if errorlevel 1 (
        echo [INFO] No changes to commit
        set SKIP_PUSH=1
        goto :skip_commit
    )
)

echo [4/5] Committing changes...
git commit -m "%commit_msg%"
set GIT_COMMIT_ERROR=%errorlevel%
if %GIT_COMMIT_ERROR% neq 0 (
    echo [WARNING] Commit failed, may be no changes to commit
    echo Checking git status...
    git status
    echo.
    echo [INFO] If there are no changes, push will be skipped
    set SKIP_PUSH=1
) else (
    set SKIP_PUSH=0
    echo [OK] Commit successful
)

:skip_commit
if "%SKIP_PUSH%"=="1" (
    echo [INFO] Skipping push - no changes to commit
    goto :skip_push
)

echo.
echo [5/5] Pushing to GitHub...
git push origin %CURRENT_BRANCH%
set GIT_PUSH_ERROR=%errorlevel%

if %GIT_PUSH_ERROR% neq 0 (
    echo.
    echo [WARNING] Push failed
    set /p force_confirm=Force push? (y/n): 
    if /i "!force_confirm!"=="y" (
        git push origin %CURRENT_BRANCH% --force
        set GIT_FORCE_ERROR=%errorlevel%
        if !GIT_FORCE_ERROR! neq 0 (
            echo [ERROR] Force push also failed
            echo Please check your network connection and GitHub credentials
            pause
            exit /b 1
        )
    ) else (
        echo Push cancelled
        pause
        exit /b 1
    )
)

:skip_push
echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo [Summary]
echo - All source code files committed
echo - Compiled files excluded (target/, node_modules/.vite/)
echo - Code pushed to GitHub
echo.
echo Repository: https://github.com/Wayneflash/ThingsLink
echo.
echo [Tip] Run git-pull.bat on another computer to sync code
echo.
pause
