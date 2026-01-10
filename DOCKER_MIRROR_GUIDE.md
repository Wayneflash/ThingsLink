# Docker 镜像加速配置指南

## 📋 概述

本指南提供多种 Docker 镜像加速配置方法，解决国内 Docker Hub 镜像拉取慢或失败的问题。

## 🔧 方法一：配置 Docker Daemon（推荐）

### 1. 创建或编辑 Docker 配置文件

```bash
# 创建配置目录（如果不存在）
sudo mkdir -p /etc/docker

# 编辑配置文件
sudo nano /etc/docker/daemon.json
```

### 2. 添加镜像加速器配置

```json
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
  }
}
```

### 3. 重启 Docker 服务

```bash
# 重新加载配置
sudo systemctl daemon-reload

# 重启 Docker
sudo systemctl restart docker

# 验证配置
docker info | grep -A 10 "Registry Mirrors"
```

## 🔧 方法二：使用阿里云容器镜像服务

### 1. 登录阿里云容器镜像服务

访问：https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors

### 2. 获取专属加速地址

登录后可以看到您的专属加速地址，例如：
```
https://abcd1234.mirror.aliyuncs.com
```

### 3. 配置 Docker Daemon

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://abcd1234.mirror.aliyuncs.com"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 🔧 方法三：使用腾讯云镜像加速器

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 🔧 方法四：使用网易镜像加速器

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://hub-mirror.c.163.com"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 🔧 方法五：使用中科大镜像加速器

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 🔧 方法六：手动拉取并重新标记镜像

如果镜像加速器都不可用，可以手动拉取并重新标记镜像：

```bash
# 1. 从阿里云镜像拉取
docker pull registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0

# 2. 重新标记为本地镜像
docker tag registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0 mysql:8.0

# 3. 删除原始镜像（可选）
docker rmi registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0

# 4. 修改 docker-compose.yml 使用本地镜像
# 将 image: registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0
# 改为 image: mysql:8.0
```

## 🔧 方法七：使用 Dockerfile 构建本地镜像

如果所有镜像源都不可用，可以构建本地镜像：

### 1. 创建 MySQL Dockerfile

```dockerfile
FROM registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0

LABEL maintainer="your-email@example.com"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置字符集
RUN echo "character-set-server=utf8mb4" >> /etc/mysql/conf.d/charset.cnf && \
    echo "collation-server=utf8mb4_unicode_ci" >> /etc/mysql/conf.d/charset.cnf
```

### 2. 构建镜像

```bash
docker build -t iot-mysql:8.0 .
```

### 3. 修改 docker-compose.yml

```yaml
services:
  mysql:
    image: iot-mysql:8.0  # 使用本地构建的镜像
    # ... 其他配置
```

## 🔧 方法八：使用 Docker Hub 代理服务

### 1. 使用 Cloudflare Workers 代理

```bash
# 配置 Docker Daemon
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.1panel.live"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

## 📝 IoT 平台项目镜像配置

### 本项目已配置的镜像源

本项目的 [`docker-compose.yml`](./docker-compose.yml) 已配置使用阿里云镜像源：

```yaml
services:
  mysql:
    image: registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0
  
  redis:
    image: registry.cn-hangzhou.aliyuncs.com/library/redis:6.2-alpine
  
  emqx:
    image: registry.cn-hangzhou.aliyuncs.com/library/emqx:5.3
```

### 如果阿里云镜像不可用

如果阿里云镜像源不可用，可以修改 [`docker-compose.yml`](./docker-compose.yml) 使用其他镜像源：

```yaml
services:
  mysql:
    image: dockerhub.icu/library/mysql:8.0
  
  redis:
    image: dockerhub.icu/library/redis:6.2-alpine
  
  emqx:
    image: dockerhub.icu/library/emqx:5.3
```

或者使用官方镜像（需要配置 Docker Daemon 镜像加速器）：

```yaml
services:
  mysql:
    image: mysql:8.0
  
  redis:
    image: redis:6.2-alpine
  
  emqx:
    image: emqx/emqx:5.3
```

## 🚀 快速部署脚本

### 一键配置 Docker 镜像加速器

```bash
#!/bin/bash

# 创建 Docker 配置目录
sudo mkdir -p /etc/docker

# 配置多个镜像加速器
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://dockerhub.icu",
    "https://docker.anyhub.us.kg",
    "https://docker.chenby.cn",
    "https://docker.awsl9527.cn"
  ]
}
EOF

# 重启 Docker
sudo systemctl daemon-reload
sudo systemctl restart docker

# 验证配置
echo "=========================================="
echo "Docker 镜像加速器配置完成！"
echo "=========================================="
docker info | grep -A 10 "Registry Mirrors"
```

保存为 `setup-docker-mirror.sh` 并执行：

```bash
chmod +x setup-docker-mirror.sh
sudo ./setup-docker-mirror.sh
```

## 🐛 故障排查

### 1. 检查 Docker 服务状态

```bash
sudo systemctl status docker
```

### 2. 查看 Docker 日志

```bash
sudo journalctl -u docker -n 50
```

### 3. 测试镜像拉取

```bash
# 测试拉取一个小镜像
docker pull hello-world

# 如果失败，查看详细错误
docker pull hello-world --debug
```

### 4. 检查网络连接

```bash
# 测试 DNS 解析
nslookup docker.io

# 测试网络连通性
ping docker.io

# 测试镜像加速器连通性
curl -I https://docker.m.daocloud.io
```

### 5. 清理 Docker 缓存

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的卷
docker volume prune

# 清理所有未使用的资源
docker system prune -a --volumes
```

## 📚 参考资源

- [Docker 官方文档 - Registry mirrors](https://docs.docker.com/registry/recipes/mirror/)
- [阿里云容器镜像服务](https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors)
- [腾讯云容器镜像服务](https://cloud.tencent.com/document/product/457/9113)
- [DaoCloud 镜像加速](https://www.daocloud.io/mirror)
- [1Panel 镜像加速](https://1panel.cn/docs/faq/docker_mirror/)

## 💡 最佳实践

1. **优先使用官方镜像**：配置 Docker Daemon 镜像加速器后，使用官方镜像名称
2. **多镜像源备份**：配置多个镜像加速器，提高可用性
3. **定期更新镜像**：定期拉取最新镜像，确保安全性和功能
4. **使用镜像标签**：使用明确的版本标签，避免使用 `latest`
5. **监控镜像拉取速度**：选择速度最快的镜像加速器

## 🆘 获取帮助

如果遇到问题，请：

1. 检查 Docker 服务状态
2. 查看 Docker 日志
3. 验证镜像加速器配置
4. 测试网络连接
5. 清理 Docker 缓存

如果问题仍然存在，请提供以下信息：

```bash
# Docker 版本
docker --version

# Docker 信息
docker info

# Docker 日志
sudo journalctl -u docker -n 100

# 网络测试
ping -c 4 docker.io
nslookup docker.io
```
