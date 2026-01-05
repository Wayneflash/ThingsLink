@echo off
chcp 65001 >nul
echo ========================================
echo   ThingsLink Git 拉取更新脚本
echo ========================================
echo.

echo [1/2] 拉取最新代码...
git pull origin main

if errorlevel 1 (
    echo.
    echo [警告] 拉取失败，尝试强制更新...
    git fetch origin main
    git reset --hard origin/main
)

echo.
echo [2/2] 显示当前状态...
git status

echo.
echo ========================================
echo   更新完成！
echo ========================================
echo.
pause
