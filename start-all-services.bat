@echo off
chcp 936 >nul
echo ========================================
echo   启动所有服务 (Docker+后端+前端)
echo ========================================
echo.

REM 自动检测脚本所在目录
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo [1/4] 启动Docker服务（自动恢复数据库）...
REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未运行，请先启动 Docker Desktop
    echo 等待 Docker 启动...
    timeout /t 5 /nobreak >nul
    
    REM 再次检查
    docker info >nul 2>&1
    if errorlevel 1 (
        echo [错误] Docker 仍未运行，请手动启动 Docker Desktop
        echo 跳过Docker启动，继续其他步骤...
        goto :skip_docker
    )
)

echo [OK] Docker 已运行

echo [INFO] 启动Docker容器...
docker-compose up -d >nul 2>&1

if errorlevel 1 (
    echo [警告] Docker启动失败，继续其他步骤...
    goto :skip_docker
)

echo [INFO] 等待MySQL启动...
timeout /t 5 /nobreak >nul

REM 等待MySQL完全启动
:wait_mysql
docker exec iot-mysql mysqladmin ping -uroot -proot123456 --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :wait_mysql
)
echo [OK] MySQL已就绪

REM 检查并恢复数据库
set "BACKUP_FILE=%CD%\backups\iot_platform_latest.sql"
if exist "%BACKUP_FILE%" (
    echo [INFO] 发现数据库备份文件，正在检查数据库状态...
    
    REM 检查数据库是否为空
    for /f "tokens=*" %%i in ('docker exec iot-mysql mysql -uroot -proot123456 -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='iot_platform';" -s -N 2^>nul') do set TABLE_COUNT=%%i
    
    if "%TABLE_COUNT%"=="" set TABLE_COUNT=0
    if %TABLE_COUNT% LSS 5 (
        echo [INFO] 数据库为空，正在恢复备份...
        docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < "%BACKUP_FILE%" >nul 2>&1
        if not errorlevel 1 (
            echo [OK] 数据库恢复成功！
        ) else (
            echo [警告] 数据库恢复失败，但服务已启动
        )
    ) else (
        echo [INFO] 数据库已有数据，跳过恢复
    )
) else (
    echo [INFO] 未找到备份文件，使用初始数据库
    echo 备份文件路径: %BACKUP_FILE%
)

echo [OK] Docker服务已启动

:skip_docker

echo.
echo [2/4] 启动后端服务...
set "BACKEND_DIR=%CD%\backend"

REM 设置Java环境变量
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot" (
        set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set JAVA_HOME=C:\Program Files\Java\jdk-17
    )
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

REM 停止旧的8080端口进程
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

if exist "%BACKEND_DIR%\target\iot-platform.jar" (
    cd /d "%BACKEND_DIR%"
    start "IOT Backend" cmd /k "java -jar target\iot-platform.jar"
    echo [OK] 后端服务已启动
) else (
    echo [警告] 找不到后端jar文件，跳过后端启动
    echo 请先编译: cd backend ^&^& mvn clean package -DskipTests
)

echo.
echo [3/4] 启动前端服务...
set "FRONTEND_DIR=%CD%\frontend"

REM 停止旧的5173端口进程
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

if exist "%FRONTEND_DIR%\package.json" (
    cd /d "%FRONTEND_DIR%"
    
    REM 检查依赖
    if not exist "node_modules" (
        echo [INFO] 正在安装前端依赖...
        call npm install >nul 2>&1
    )
    
    start "IOT Frontend" cmd /k "npm run dev"
    echo [OK] 前端服务已启动
) else (
    echo [警告] 找不到前端目录，跳过前端启动
)

echo.
echo [4/4] 完成！

echo.
echo ========================================
echo   所有服务已启动！
echo ========================================
echo.
echo 服务地址：
echo   MySQL:    localhost:3306
echo   Redis:    localhost:16379
echo   EMQX:     localhost:1883 (MQTT)
echo   EMQX Web: http://localhost:18083 (admin/public)
echo   后端:     http://localhost:8080
echo   前端:     http://localhost:5173
echo.
pause

