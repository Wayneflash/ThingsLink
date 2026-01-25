@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=!SCRIPT_DIR:~0,-1!

REM 切换到项目根目录
cd /d "!SCRIPT_DIR!\.."

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未检测到Python，请先安装Python
    pause
    exit /b 1
)

REM 检查必要的库
python -c "import docx, markdown" >nul 2>&1
if errorlevel 1 (
    echo 正在安装必要的Python库...
    pip install python-docx markdown
    if errorlevel 1 (
        echo 错误：库安装失败
        pause
        exit /b 1
    )
)

REM 检查参数
if "%~1"=="" (
    echo 使用方法: md-to-word.bat ^<markdown文件路径^> [输出Word文件路径]
    echo.
    echo 示例:
    echo   md-to-word.bat docs\产品宣传文档.md
    echo   md-to-word.bat docs\产品宣传文档.md output.docx
    pause
    exit /b 1
)

REM 运行Python脚本
python scripts\md-to-word.py %*

pause
