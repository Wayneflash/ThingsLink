@echo off
chcp 65001 >nul
echo ========================================
echo   Git 拉取代码
echo ========================================
echo.

echo [1/2] 拉取最新代码...
git pull origin main
if errorlevel 1 (
    echo ❌ 拉取代码失败
    pause
    exit /b 1
)
echo ✅ 代码拉取完成
echo.

echo [2/2] 显示当前状态...
git status
echo.

echo ========================================
echo   ✅ 完成
echo ========================================
pause
