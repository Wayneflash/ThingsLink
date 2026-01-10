#!/bin/bash

################################################################################
# 部署到云服务器脚本
# 用于将本地编译的文件上传到服务器并部署
################################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印分隔线
print_separator() {
    echo -e "${BLUE}============================================${NC}"
}

# 打印信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

# 打印警告
print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 打印错误
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 打印成功
print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 检查必要的命令
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 未安装，请先安装 $1"
        exit 1
    fi
}

# 检查必要的命令
check_commands() {
    print_info "检查必要的命令..."
    check_command "scp"
    check_command "ssh"
    print_success "所有必要命令已安装"
}

# 配置服务器信息
SERVER_IP=""
SERVER_USER="root"
SERVER_PASSWORD=""
SERVER_PATH="/root/things-link"

# 从用户输入获取服务器信息
get_server_info() {
    print_separator
    echo -e "${BLUE}配置服务器信息${NC}"
    print_separator
    echo ""
    
    read -p "请输入服务器IP地址: " SERVER_IP
    read -p "请输入服务器用户名 [默认: root]: " SERVER_USER
    SERVER_USER=${SERVER_USER:-root}
    read -s -p "请输入服务器密码: " SERVER_PASSWORD
    echo ""
    
    print_info "服务器信息："
    echo "  IP: $SERVER_IP"
    echo "  用户: $SERVER_USER"
    echo "  路径: $SERVER_PATH"
    echo ""
}

# 上传后端 jar 文件
upload_backend() {
    print_separator
    print_info "上传后端 jar 文件..."
    print_separator
    
    if [ ! -f "backend/target/iot-platform.jar" ]; then
        print_error "后端 jar 文件不存在，请先执行: cd backend && mvn clean package -DskipTests"
        exit 1
    fi
    
    # 创建服务器目录
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} "mkdir -p $SERVER_PATH/backend"
    
    # 上传 jar 文件
    sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=no backend/target/iot-platform.jar ${SERVER_USER}@${SERVER_IP}:$SERVER_PATH/backend/
    
    print_success "后端 jar 文件上传完成"
}

# 上传前端 dist 文件
upload_frontend() {
    print_separator
    print_info "上传前端 dist 文件..."
    print_separator
    
    if [ ! -d "frontend/dist" ]; then
        print_error "前端 dist 目录不存在，请先执行: cd frontend && npm run build"
        exit 1
    fi
    
    # 创建服务器目录
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} "mkdir -p $SERVER_PATH/frontend/dist"
    
    # 上传 dist 文件
    sshpass -p "$SERVER_PASSWORD" scp -r -o StrictHostKeyChecking=no frontend/dist/* ${SERVER_USER}@${SERVER_IP}:$SERVER_PATH/frontend/dist/
    
    print_success "前端 dist 文件上传完成"
}

# 初始化数据库
init_database() {
    print_separator
    print_info "初始化数据库..."
    print_separator
    
    # 检查数据库是否已初始化
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} "docker exec iot-mysql mysql -uroot -proot123456 -e 'SELECT 1 FROM tb_user LIMIT 1;' 2>/dev/null"
    
    if [ $? -eq 0 ]; then
        print_warning "数据库已初始化，跳过"
    else
        print_info "初始化数据库..."
        sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} "docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < $SERVER_PATH/init.sql"
        print_success "数据库初始化完成"
    fi
}

# 启动 Docker 基础服务
start_docker_services() {
    print_separator
    print_info "启动 Docker 基础服务（MySQL、Redis、EMQX）..."
    print_separator
    
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} << 'ENDSSH'
        cd /root/things-link
        docker-compose up -d mysql redis emqx
ENDSSH
    
    print_success "Docker 基础服务启动完成"
}

# 启动后端服务
start_backend() {
    print_separator
    print_info "启动后端服务..."
    print_separator
    
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} << 'ENDSSH'
        cd /root/things-link/backend
        # 停止旧的后端进程
        pkill -f "iot-platform.jar" || true
        sleep 2
        # 启动后端
        nohup java -jar iot-platform.jar > backend.log 2>&1 &
ENDSSH
    
    print_success "后端服务启动完成"
}

# 安装并启动 Nginx
start_frontend() {
    print_separator
    print_info "安装并启动 Nginx..."
    print_separator
    
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} << 'ENDSSH'
        # 检查 Nginx 是否已安装
        if ! command -v nginx &> /dev/null; then
            echo "安装 Nginx..."
            apt update
            apt install -y nginx
        fi
        
        # 复制前端文件到 Nginx 目录
        rm -rf /var/www/html/*
        cp -r /root/things-link/frontend/dist/* /var/www/html/
        
        # 配置 Nginx
        cat > /etc/nginx/sites-available/default << 'EOF'
server {
    listen 80;
    server_name _;
    
    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
        
        # 重启 Nginx
        systemctl restart nginx
        systemctl enable nginx
ENDSSH
    
    print_success "Nginx 安装并启动完成"
}

# 显示访问地址
show_access_info() {
    print_separator
    print_info "部署完成！访问地址："
    print_separator
    echo ""
    echo -e "${GREEN}前端界面${NC}: http://$SERVER_IP:80"
    echo -e "${GREEN}后端API${NC}: http://$SERVER_IP:8080"
    echo -e "${GREEN}EMQX Dashboard${NC}: http://$SERVER_IP:18083"
    echo ""
    echo -e "${YELLOW}默认账号${NC}: admin / admin123456"
    echo ""
}

# 查看服务状态
check_services_status() {
    print_separator
    print_info "检查服务状态..."
    print_separator
    
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} << 'ENDSSH'
        echo "=== Docker 容器状态 ==="
        docker-compose ps
        echo ""
        echo "=== 后端进程 ==="
        ps aux | grep iot-platform.jar || echo "后端未运行"
        echo ""
        echo "=== Nginx 状态 ==="
        systemctl status nginx --no-pager || echo "Nginx 未运行"
ENDSSH
}

# 主函数
main() {
    print_separator
    echo -e "${GREEN}IoT物联网设备管理平台 - 部署到云服务器${NC}"
    print_separator
    echo ""
    
    # 检查必要命令
    check_commands
    
    # 获取服务器信息
    get_server_info
    
    # 上传文件
    upload_backend
    upload_frontend
    
    # 初始化数据库
    init_database
    
    # 启动服务
    start_docker_services
    start_backend
    start_frontend
    
    # 显示访问信息
    show_access_info
    
    # 检查服务状态
    check_services_status
    
    print_separator
    print_success "部署完成！"
    print_separator
}

# 执行主函数
main
