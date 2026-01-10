#!/bin/bash

################################################################################
# Docker 镜像加速器一键配置脚本
# 用于解决国内 Docker Hub 镜像拉取慢或失败的问题
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

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then 
        print_error "请使用 sudo 运行此脚本"
        echo "使用方法: sudo $0"
        exit 1
    fi
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装"
        print_info "请先运行 install-docker.sh 安装 Docker"
        exit 1
    fi
    print_success "Docker 已安装"
}

# 备份现有配置
backup_config() {
    if [ -f /etc/docker/daemon.json ]; then
        print_info "备份现有 Docker 配置..."
        cp /etc/docker/daemon.json /etc/docker/daemon.json.backup.$(date +%Y%m%d_%H%M%S)
        print_success "配置已备份到 /etc/docker/daemon.json.backup.$(date +%Y%m%d_%H%M%S)"
    fi
}

# 创建 Docker 配置目录
create_config_dir() {
    print_info "创建 Docker 配置目录..."
    mkdir -p /etc/docker
    print_success "配置目录已创建"
}

# 配置镜像加速器
configure_mirrors() {
    print_info "配置 Docker 镜像加速器..."
    
    # 写入配置文件
    cat > /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://dockerhub.icu",
    "https://docker.anyhub.us.kg",
    "https://docker.chenby.cn",
    "https://docker.awsl9527.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF
    
    print_success "镜像加速器配置完成"
}

# 重启 Docker 服务
restart_docker() {
    print_info "重新加载 Docker 配置..."
    systemctl daemon-reload
    
    print_info "重启 Docker 服务..."
    systemctl restart docker
    
    # 等待 Docker 启动
    sleep 3
    
    # 检查 Docker 状态
    if systemctl is-active --quiet docker; then
        print_success "Docker 服务重启成功"
    else
        print_error "Docker 服务重启失败"
        print_info "查看 Docker 状态: sudo systemctl status docker"
        print_info "查看 Docker 日志: sudo journalctl -u docker -n 50"
        exit 1
    fi
}

# 验证配置
verify_config() {
    print_info "验证镜像加速器配置..."
    
    # 显示配置的镜像加速器
    echo ""
    print_separator
    print_info "已配置的镜像加速器："
    print_separator
    docker info | grep -A 10 "Registry Mirrors" || print_warning "未找到镜像加速器配置"
    print_separator
    echo ""
}

# 测试镜像拉取
test_pull() {
    print_info "测试镜像拉取..."
    
    print_info "拉取 hello-world 镜像..."
    if docker pull hello-world > /dev/null 2>&1; then
        print_success "镜像拉取测试成功"
        docker rmi hello-world > /dev/null 2>&1 || true
    else
        print_warning "镜像拉取测试失败"
        print_info "这可能是因为网络问题或镜像加速器暂时不可用"
        print_info "请稍后重试或手动配置其他镜像加速器"
    fi
}

# 显示使用说明
show_usage() {
    print_separator
    print_info "使用说明："
    print_separator
    echo ""
    echo "1. 查看配置的镜像加速器："
    echo "   docker info | grep -A 10 'Registry Mirrors'"
    echo ""
    echo "2. 测试镜像拉取："
    echo "   docker pull hello-world"
    echo ""
    echo "3. 如果需要修改配置："
    echo "   sudo nano /etc/docker/daemon.json"
    echo "   sudo systemctl restart docker"
    echo ""
    echo "4. 查看详细的镜像加速配置指南："
    echo "   cat DOCKER_MIRROR_GUIDE.md"
    echo ""
    print_separator
}

# 主函数
main() {
    print_separator
    echo -e "${GREEN}Docker 镜像加速器一键配置脚本${NC}"
    print_separator
    echo ""
    
    # 检查权限
    check_root
    
    # 检查 Docker
    check_docker
    
    # 备份现有配置
    backup_config
    
    # 创建配置目录
    create_config_dir
    
    # 配置镜像加速器
    configure_mirrors
    
    # 重启 Docker
    restart_docker
    
    # 验证配置
    verify_config
    
    # 测试镜像拉取
    test_pull
    
    # 显示使用说明
    show_usage
    
    print_separator
    print_success "Docker 镜像加速器配置完成！"
    print_separator
}

# 执行主函数
main
