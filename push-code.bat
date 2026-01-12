@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

REM 获取脚本所在目录（自动适配不同路径）
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到脚本所在目录
cd /d "!SCRIPT_DIR!"

echo ========================================
echo   Git 推送脚本（一键提交并推送）
echo ========================================
echo.
echo 项目路径: !SCRIPT_DIR!
echo.
echo 功能说明:
echo   - 自动添加所有本地更改
echo   - 自动提交更改（带时间戳）
echo   - 自动推送到GitHub（覆盖远程）
echo   你只需要双击运行，无需手动执行git命令！
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
REM 生成时间戳格式的提交消息
for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set DATE_PART=%%c-%%a-%%b
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set TIME_PART=%%a:%%b
set COMMIT_MSG=Auto commit: !DATE_PART! !TIME_PART!
git commit -m "!COMMIT_MSG!"
if errorlevel 1 (
    echo [ERROR] Failed to commit
    echo 可能原因: 没有需要提交的更改
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
echo - ✅ 所有本地更改已自动提交
echo - ✅ 代码已推送到远程: origin/%CURRENT_BRANCH%
echo - ✅ 远程内容已被本地版本覆盖
echo.
echo 提示: 你只需要双击这个脚本，就会自动完成：
echo   1. 检查更改状态
echo   2. 添加所有更改到暂存区
echo   3. 自动提交更改（带时间戳）
echo   4. 强制推送到GitHub（覆盖远程）
echo.
pause
endlocal
