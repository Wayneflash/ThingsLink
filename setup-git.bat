@echo off
chcp 65001 >nul
echo ========================================
echo   ThingsLink Git 环境配置脚本
echo ========================================
echo.

REM 检查Git是否安装
where git >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Git，请先安装Git
    echo 下载地址: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo [1/4] 配置Git用户信息...
git config --global user.name "Wayneflash"
git config --global user.email "wangyongqiang1992@gmail.com"
echo ✓ 用户信息配置完成

echo.
echo [2/4] 生成SSH密钥...
if exist "%USERPROFILE%\.ssh\id_ed25519" (
    echo ⚠ SSH密钥已存在，跳过生成
) else (
    ssh-keygen -t ed25519 -C "wangyongqiang1992@gmail.com" -f "%USERPROFILE%\.ssh\id_ed25519" -N ""
    echo ✓ SSH密钥生成完成
)

echo.
echo [3/4] 显示公钥...
echo ========================================
type "%USERPROFILE%\.ssh\id_ed25519.pub"
echo ========================================
echo.
echo 👆 请复制上面的公钥，添加到GitHub:
echo    https://github.com/settings/keys
echo.
pause

echo.
echo [4/4] 克隆仓库...
set /p clone_path=请输入克隆到的目录路径 (如: D:\Projects): 

if "%clone_path%"=="" (
    echo [错误] 路径不能为空
    pause
    exit /b 1
)

cd /d "%clone_path%"
if exist "ThingsLink" (
    echo ⚠ 目录已存在，跳过克隆
) else (
    git clone git@github.com:Wayneflash/ThingsLink.git
    if errorlevel 1 (
        echo [错误] 克隆失败，请确认SSH密钥已添加到GitHub
        pause
        exit /b 1
    )
    echo ✓ 仓库克隆完成
)

echo.
echo ========================================
echo   配置完成！
echo ========================================
echo.
echo 项目路径: %clone_path%\ThingsLink
echo.
echo 下一步:
echo 1. 进入项目目录
echo 2. 运行 git-pull.bat 拉取最新代码
echo 3. 运行 git-push.bat 提交代码
echo.
pause
