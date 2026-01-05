@echo off
setlocal enabledelayedexpansion
echo ========================================
echo   Git Push Script (Backup+Commit+Push)
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
set /p commit_msg=Enter commit message (press Enter for default): 

if "%commit_msg%"=="" (
    set commit_msg=Update code
)

REM Auto backup database (if Docker is running)
echo.
echo [0/4] Checking if database backup is needed...
REM Check if Docker is available
where docker >nul 2>&1
if errorlevel 1 (
    echo [INFO] Docker not installed or not in PATH, skipping database backup
    goto :skip_backup
)

docker ps --filter "name=iot-mysql" --format "{{.Names}}" 2>nul | findstr /i "iot-mysql" >nul 2>&1
if not errorlevel 1 (
    echo [INFO] MySQL container is running, backing up database...
    set "BACKUP_DIR=%CD%\backups"
    if not exist "%BACKUP_DIR%" (
        mkdir "%BACKUP_DIR%" 2>nul
    )
    set "BACKUP_FILE=%BACKUP_DIR%\iot_platform_latest.sql"
    
    docker exec iot-mysql mysqldump -uroot -proot123456 iot_platform > "%BACKUP_FILE%" 2>nul
    if not errorlevel 1 (
        echo [OK] Database backup completed
    ) else (
        echo [WARNING] Database backup failed, continuing with code push
    )
) else (
    echo [INFO] MySQL container is not running, skipping database backup
)
:skip_backup

echo.
echo [1/4] Adding changes...
git add .
set GIT_ADD_ERROR=%errorlevel%
if %GIT_ADD_ERROR% neq 0 (
    echo [ERROR] Git add failed
    echo Please check if you are in a valid Git repository
    pause
    exit /b 1
)

echo [2/4] Committing...
git commit -m "%commit_msg%"
set GIT_COMMIT_ERROR=%errorlevel%
if %GIT_COMMIT_ERROR% neq 0 (
    echo [WARNING] Commit failed, may be no changes to commit
    echo Continuing with push...
)

echo [3/4] Pushing to GitHub...
git push origin main
set GIT_PUSH_ERROR=%errorlevel%

if %GIT_PUSH_ERROR% neq 0 (
    echo.
    echo [WARNING] Push failed
    set /p force_confirm=Force push? (y/n): 
    if /i "!force_confirm!"=="y" (
        git push origin main --force
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

echo.
echo [4/4] Push completed!

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo Repository: https://github.com/Wayneflash/ThingsLink
echo.
pause
