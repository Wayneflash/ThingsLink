#!/bin/bash

# Docker 和 Docker Compose 安装脚本
# 适用于 Ubuntu/Debian/CentOS 系统

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 检测操作系统
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VERSION=$VERSION_ID
    else
        print_error "无法检测操作系统"
        exit 1
    fi
    print_info "检测到操作系统: $OS $VERSION"
}

# 安装 Docker (Ubuntu/Debian)
install_docker_ubuntu() {
    print_info "更新软件包索引..."
    sudo apt-get update

    print_info "安装依赖包..."
    sudo apt-get install -y \
        ca-certificates \
        curl \
        gnupg \
        lsb-release

    print_info "添加 Docker 官方 GPG 密钥..."
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

    print_info "设置 Docker 仓库..."
    echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
        $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    print_info "安装 Docker Engine..."
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    print_success "Docker 安装完成"
}

# 安装 Docker (CentOS/RHEL)
install_docker_centos() {
    print_info "安装依赖包..."
    sudo yum install -y yum-utils

    print_info "添加 Docker 仓库..."
    sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

    print_info "安装 Docker Engine..."
    sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    print_success "Docker 安装完成"
}

# 启动 Docker 服务
start_docker() {
    print_info "启动 Docker 服务..."
    sudo systemctl start docker
    sudo systemctl enable docker

    print_success "Docker 服务已启动"
}

# 验证 Docker 安装
verify_docker() {
    print_info "验证 Docker 安装..."
    sudo docker --version
    sudo docker compose version

    print_success "Docker 验证通过"
}

# 配置 Docker 用户权限（可选）
configure_docker_user() {
    print_info "配置当前用户使用 Docker（无需 sudo）..."
    sudo usermod -aG docker $USER

    print_warning "请执行以下命令使权限生效："
    print_warning "  newgrp docker"
    print_warning "或者注销后重新登录"
}

# 主函数
main() {
    echo "========================================"
    echo "Docker 和 Docker Compose 安装脚本"
    echo "========================================"
    echo ""

    # 检测操作系统
    detect_os

    # 根据操作系统安装 Docker
    case $OS in
        ubuntu|debian)
            install_docker_ubuntu
            ;;
        centos|rhel|fedora)
            install_docker_centos
            ;;
        *)
            print_error "不支持的操作系统: $OS"
            print_info "请手动安装 Docker 和 Docker Compose"
            exit 1
            ;;
    esac

    # 启动 Docker 服务
    start_docker

    # 验证 Docker 安装
    verify_docker

    # 配置用户权限
    configure_docker_user

    echo ""
    print_success "========================================"
    print_success "安装完成！"
    print_success "========================================"
    echo ""
    print_info "现在可以运行部署脚本："
    print_info "  ./deploy.sh"
    echo ""
}

# 运行主函数
main
