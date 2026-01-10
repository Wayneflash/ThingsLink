#!/bin/bash

# IoT物联网设备管理平台 - 一键部署脚本
# 版本: 1.0.0
# 说明: 自动化部署整个IoT平台（MySQL + Redis + EMQX + 后端 + 前端）

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 打印分隔线
print_separator() {
    echo "============================================================================"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 命令未找到，请先安装: sudo apt-get install $1"
        exit 1
    fi
}

# 检查端口是否被占用
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "端口 $1 已被占用，将停止占用进程"
        lsof -ti :$1 -sTCP:LISTEN | xargs kill -9
        sleep 2
    fi
}

# 主函数
main() {
    print_separator
    echo "IoT物联网设备管理平台 - 一键部署脚本"
    print_separator
    echo ""
    
    # 1. 检查必要的命令
    print_info "检查必要的命令..."
    check_command "docker"
    check_command "docker-compose"
    
    # 2. 检查端口占用
    print_info "检查端口占用情况..."
    check_port 3306   # MySQL
    check_port 6379   # Redis
    check_port 1883   # EMQX MQTT
    check_port 8083   # EMQX WebSocket
    check_port 8883   # EMQX Dashboard
    
    # 3. 停止并删除旧容器
    print_info "停止并删除旧容器..."
    docker-compose down 2>/dev/null || true
    
    # 4. 创建必要的目录
    print_info "创建必要的目录..."
    mkdir -p mysql-data emqx-data emqx-log
    
    # 5. 启动所有服务
    print_separator
    print_info "启动所有服务..."
    print_separator
    
    # 使用docker-compose启动服务
    docker-compose up -d
    
    # 6. 等待服务启动
    print_info "等待服务启动..."
    sleep 5
    
    # 7. 检查服务状态
    print_separator
    print_info "检查服务状态..."
    print_separator
    
    # 检查MySQL
    if docker ps | grep -q "iot-mysql"; then
        print_success "MySQL 容器已启动"
    else
        print_error "MySQL 容器启动失败"
        docker-compose logs mysql
        exit 1
    fi
    
    # 检查Redis
    if docker ps | grep -q "iot-redis"; then
        print_success "Redis 容器已启动"
    else
        print_error "Redis 容器启动失败"
        docker-compose logs redis
        exit 1
    fi
    
    # 检查EMQX
    if docker ps | grep -q "iot-emqx"; then
        print_success "EMQX 容器已启动"
    else
        print_error "EMQX 容器启动失败"
        docker-compose logs emqx
        exit 1
    fi
    
    # 8. 等待MySQL完全启动
    print_info "等待MySQL完全启动..."
    sleep 10
    
    # 9. 初始化数据库
    print_separator
    print_info "初始化数据库..."
    print_separator
    docker exec iot-mysql mysql -uroot -proot123456 iot_platform < /docker-entrypoint-initdb.d/init.sql 2>/dev/null
    
    if [ $? -eq 0 ]; then
        print_success "数据库初始化成功"
    else
        print_error "数据库初始化失败"
        exit 1
    fi
    
    # 10. 显示部署完成信息
    print_separator
    print_success "========================================"
    print_success "部署完成！"
    print_success "========================================"
    print_separator
    echo ""
    print_info "服务访问地址："
    print_info "  - MySQL: localhost:3306 (root/root123456)"
    print_info "  - Redis: localhost:6379 (redis123456)"
    print_info "  - EMQX MQTT: localhost:1883"
    print_info "  - EMQX Dashboard: http://localhost:18083"
    print_info "  - 后端API: http://localhost:8080"
    print_info "  - 前端页面: http://localhost:8080"
    print_separator
    echo ""
    print_info "查看容器日志命令："
    print_info "  docker-compose logs -f [mysql|redis|emqx]"
    print_info "  docker-compose logs mysql"
    print_info "  docker-compose logs redis"
    print_info "  docker-compose logs emqx"
    print_separator
    echo ""
    print_warning "首次部署后，请等待30秒让所有服务完全启动"
    print_separator
}

# 运行主函数
main
