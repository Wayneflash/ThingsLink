@echo off
chcp 65001 >nul
echo ========================================
echo   Git 推送代码
echo ========================================
echo.

echo [1/4] 显示当前状态...
git status
echo.

echo [2/4] 添加所有更改...
git add .
if errorlevel 1 (
    echo ❌ 添加文件失败
    pause
    exit /b 1
)
echo ✅ 文件已添加到暂存区
echo.

echo [3/4] 提交更改...
set /p COMMIT_MSG="请输入提交信息: "
if "%COMMIT_MSG%"=="" (
    set COMMIT_MSG=更新代码
)
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo ⚠️  提交可能失败（可能没有更改）
)
echo ✅ 提交完成
echo.

echo [4/4] 推送到远程仓库...
git push origin main
if errorlevel 1 (
    echo ❌ 推送失败
    pause
    exit /b 1
)
echo ✅ 推送完成
echo.

echo ========================================
echo   ✅ 完成
echo ========================================
pause
