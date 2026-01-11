@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   ThingsLink 自动部署脚本 (Windows)
echo ========================================
echo.

set SERVER_IP=117.72.222.8
set SERVER_USER=root
set SERVER_PATH=/root/things-link
set SERVER_PASSWORD=Qiang531..

echo [1/7] 检查编译文件...
if not exist "backend\target\iot-platform.jar" (
    echo ❌ 错误: 后端 jar 文件不存在
    echo 请先执行: cd backend ^&^& mvn clean package -DskipTests
    pause
    exit /b 1
)

if not exist "frontend\dist" (
    echo ❌ 错误: 前端 dist 目录不存在
    echo 请先执行: cd frontend ^&^& npm install ^&^& npm run build
    pause
    exit /b 1
)

echo ✅ 编译文件检查完成
echo.

echo [2/7] 上传后端 jar 文件到服务器...
scp backend\target\iot-platform.jar %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/backend/
if errorlevel 1 (
    echo ❌ 上传后端文件失败
    pause
    exit /b 1
)
echo ✅ 后端文件上传完成
echo.

echo [3/7] 上传前端 dist 文件到服务器...
scp -r frontend\dist %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/frontend/
if errorlevel 1 (
    echo ❌ 上传前端文件失败
    pause
    exit /b 1
)
echo ✅ 前端文件上传完成
echo.

echo [4/7] 初始化数据库...
scp init.sql %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/
ssh %SERVER_USER%@%SERVER_IP% "cd %SERVER_PATH% && docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < init.sql"
if errorlevel 1 (
    echo ⚠️  数据库初始化可能失败，但继续部署
)
echo ✅ 数据库初始化完成
echo.

echo [5/7] 启动 Docker 基础服务...
ssh %SERVER_USER%@%SERVER_IP% "cd %SERVER_PATH% && docker-compose up -d mysql redis emqx"
if errorlevel 1 (
    echo ❌ 启动 Docker 服务失败
    pause
    exit /b 1
)
echo ✅ Docker 基础服务启动完成
echo.

echo [6/7] 启动后端服务...
ssh %SERVER_USER%@%SERVER_IP% "cd %SERVER_PATH%/backend && pkill -f 'iot-platform.jar' || true && sleep 2 && nohup java -jar iot-platform.jar > backend.log 2>&1 &"
if errorlevel 1 (
    echo ❌ 启动后端服务失败
    pause
    exit /b 1
)
echo ✅ 后端服务启动完成
echo.

echo [7/7] 安装并启动 Nginx...
ssh %SERVER_USER%@%SERVER_IP% "command -v nginx > /dev/null 2>&1 || (apt update && apt install -y nginx) && rm -rf /var/www/html/* && cp -r %SERVER_PATH%/frontend/dist/* /var/www/html/ && cat > /etc/nginx/sites-available/default << 'EOF'
server {
    listen 80;
    server_name _;
    
    location / {
        root /var/www/html;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF
 && systemctl restart nginx && systemctl enable nginx"
if errorlevel 1 (
    echo ❌ 启动 Nginx 失败
    pause
    exit /b 1
)
echo ✅ Nginx 启动完成
echo.

echo ========================================
echo   🎉 部署完成！
echo ========================================
echo.
echo 访问地址:
echo   - 前端界面: http://%SERVER_IP%:80
echo   - 后端API:   http://%SERVER_IP%:8080
echo   - EMQX控制台: http://%SERVER_IP%:18083
echo.
echo 默认账号:
echo   - 系统登录: admin / admin123456
echo   - EMQX:     admin / admin123456
echo.

echo 服务状态:
ssh %SERVER_USER%@%SERVER_IP% "echo '=== Docker 服务 ===' && docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' && echo '' && echo '=== 后端进程 ===' && ps aux | grep iot-platform.jar | grep -v grep && echo '' && echo '=== Nginx 状态 ===' && systemctl is-active nginx"

echo.
pause
