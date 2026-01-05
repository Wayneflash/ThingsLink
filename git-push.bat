@echo off
chcp 65001 >nul
echo ========================================
echo   ThingsLink Git 提交脚本
echo ========================================
echo.

REM 检查是否有未提交的更改
git status --short
if errorlevel 1 (
    echo [错误] Git 仓库初始化失败
    pause
    exit /b 1
)

echo.
set /p commit_msg=请输入提交信息 (直接回车使用默认信息): 

if "%commit_msg%"=="" (
    set commit_msg=Update: 更新代码
)

echo.
echo [1/3] 添加所有更改...
git add .

echo [2/3] 提交代码...
git commit -m "%commit_msg%"

echo [3/3] 推送到 GitHub...
git push origin main

if errorlevel 1 (
    echo.
    echo [警告] 推送失败，尝试强制推送...
    git push origin main --force
)

echo.
echo ========================================
echo   提交完成！
echo ========================================
echo.
echo 仓库地址: https://github.com/Wayneflash/ThingsLink
echo.
pause
