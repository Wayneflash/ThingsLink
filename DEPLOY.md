# IoT物联网设备管理平台 - 一键部署指南

## 📋 概述

本平台采用Docker容器化部署方案，提供一键部署脚本，用户无需任何操作即可完成部署。

## 🎯 快速开始

### 前置准备（全新服务器）

如果是全新服务器，需要先安装 Docker 和 Docker Compose：

```bash
# 1. 给安装脚本添加执行权限
chmod +x install-docker.sh

# 2. 执行安装脚本
sudo ./install-docker.sh

# 3. 使权限生效（重要！）
newgrp docker
# 或者注销后重新登录
```

安装脚本会自动检测操作系统并安装：
- ✅ Docker Engine
- ✅ Docker Compose
- ✅ 配置当前用户使用 Docker（无需 sudo）

### 方法一：使用一键部署脚本（推荐）

```bash
# 1. 克隆或下载项目代码到服务器
cd /path/to/project

# 2. 给脚本添加执行权限
chmod +x deploy.sh

# 3. 执行一键部署脚本
./deploy.sh
```

### 方法二：手动执行Docker命令

```bash
# 1. 停止并删除旧容器
docker-compose down

# 2. 创建必要的目录
mkdir -p mysql-data emqx-data emqx-log

# 3. 启动所有服务
docker-compose up -d

# 4. 等待服务启动（约10秒）
sleep 10

# 5. 初始化数据库
docker exec iot-mysql mysql -uroot -proot123456 iot_platform < /docker-entrypoint-initdb.d/init.sql
```

## 📦 服务说明

### 服务列表

| 服务名称 | 容器名 | 端口 | 说明 |
|---------|---------|------|------|
| MySQL | iot-mysql | 3306 | 数据库服务 |
| Redis | iot-redis | 6379 | 缓存服务 |
| EMQX | iot-emqx | 1883, 8083, 8883, 18083 | MQTT消息服务器 |
| 后端 | backend | 8080 | Spring Boot后端API服务 |
| 前端 | frontend | 8080 | Vue 3前端页面 |

### 服务访问地址

#### 数据库服务
- **MySQL**: `localhost:3306`
  - 用户名: `root`
  - 密码: `root123456`
  - 数据库: `iot_platform`
  - 字符集: `utf8mb4`

#### 缓存服务
- **Redis**: `localhost:6379`
  - 密码: `redis123456`

#### MQTT消息服务器
- **EMQX MQTT**: `localhost:1883`
  - Dashboard: http://localhost:18083
  - MQTT端口: 1883
  - WebSocket端口: 8083
  - SSL端口: 8883

#### 应用服务
- **后端API**: `http://localhost:8080`
  - API基础路径: `/api`
  - Swagger文档: `http://localhost:8080/swagger-ui.html`

- **前端页面**: `http://localhost:8080`
  - 设备管理: `http://localhost:8080/devices`
  - 设备详情: `http://localhost:8080/devices/{deviceCode}`
  - 报警日志: `http://localhost:8080/alarms`
  - 系统设置: `http://localhost:8080/settings`

> **端口说明**：
> - **生产环境**：前端和后端都通过8080端口访问（前后端部署在一起）
> - **开发环境**：前端开发服务器运行在5173端口，后端运行在8080端口

## 🔧 系统要求

### 硬件要求
- CPU: 2核及以上
- 内存: 4GB及以上
- 磁盘: 20GB及以上可用空间
- 网络: 稳定的网络连接

### 软件要求
- **操作系统**: Linux (Ubuntu 20.04+, CentOS 7+, Debian 10+)
- **Docker**: 20.10.0及以上
- **Docker Compose**: 1.29.0及以上
- **Git**: 用于拉取代码（可选）

## 📝 配置说明

### 端口说明

如果以下端口被占用，部署脚本会自动停止占用进程：

| 端口 | 服务 | 说明 |
|-------|------|------|
| 3306 | MySQL | 数据库端口 |
| 6379 | Redis | 缓存端口 |
| 1883 | EMQX MQTT | MQTT协议端口 |
| 8083 | EMQX WebSocket | MQTT WebSocket端口 |
| 8883 | EMQX Dashboard | EMQX管理界面 |
| 18083 | EMQX Dashboard SSL | EMQX管理界面SSL |
| 8080 | 后端/前端 | 应用HTTP端口 |

### 数据库配置

数据库配置文件位置: `init.sql`

首次部署时会自动执行数据库初始化脚本，创建以下表：
- 用户表 (`tb_user`)
- 角色表 (`tb_role`)
- 设备表 (`tb_device`)
- 产品表 (`tb_product`)
- 设备分组表 (`tb_device_group`)
- 设备数据表 (`tb_device_data`)
- 告警日志表 (`tb_alarm_log`)
- 命令记录表 (`tb_command`)
- 通知表 (`tb_notification`)
- 系统配置表 (`tb_system_config`)

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | admin | admin123456 | 拥有所有权限 |

## 🚀 部署后操作

### 查看容器状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看特定容器日志
docker-compose logs mysql
docker-compose logs redis
docker-compose logs emqx
docker-compose logs backend
docker-compose logs frontend
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止特定服务
docker-compose stop mysql
docker-compose stop redis
docker-compose stop emqx
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart mysql
docker-compose restart redis
docker-compose restart emqx
```

### 清理数据

```bash
# 停止服务并删除数据卷
docker-compose down -v

# 重新部署（会重新初始化数据库）
./deploy.sh
```

## 📊 性能说明

### 支持规模
- 设备数量: 10,000台
- 并发数据上报: 约167条/秒（按1分钟1条数据计算）
- 在线用户: 100+人

### 性能优化
- MySQL使用分区表优化时序数据查询
- Redis缓存设备在线状态和最新数据
- MQTT消息异步处理，避免阻塞
- 前端使用分页查询，避免一次性加载大量数据

## 🔐 常见问题

### 问题1: 端口被占用
**现象**: 部署脚本提示端口被占用
**解决**: 脚本会自动停止占用进程，如果问题持续，手动执行：
```bash
# 查看端口占用
sudo lsof -i :3306
sudo lsof -i :6379
sudo lsof -i :1883
sudo lsof -i :8083
sudo lsof -i :8080

# 停止占用进程
sudo kill -9 <PID>
```

### 问题2: 容器启动失败
**现象**: 服务启动失败
**解决**: 查看容器日志
```bash
docker-compose logs mysql
docker-compose logs redis
docker-compose logs emqx
docker-compose logs backend
docker-compose logs frontend
```

### 问题3: 数据库连接失败
**现象**: 后端无法连接数据库
**解决**: 
1. 确认MySQL容器已启动
2. 检查数据库是否初始化
3. 查看MySQL日志: `docker-compose logs mysql`

### 问题4: 前端无法访问后端API
**现象**: 前端页面报错
**解决**:
1. 确认后端容器已启动
2. 查看后端日志: `docker-compose logs backend`
3. 确认后端端口8080是否正常

### 问题5: MQTT连接失败
**现象**: 设备无法连接MQTT服务器
**解决**:
1. 确认EMQX容器已启动
2. 查看EMQX日志: `docker-compose logs emqx`
3. 确认EMQX Dashboard可访问: http://localhost:18083

## 📖 数据备份

### 备份数据库

```bash
# 备份MySQL数据到backup目录
docker exec iot-mysql mysqldump -uroot -proot123456 iot_platform > ./backups/iot_platform_$(date +%Y%m%d_%H%M%S).sql

# 备份到本地
docker exec iot-mysql mysqldump -uroot -proot123456 iot_platform > ~/iot_platform_backup_$(date +%Y%m%d_%H%M%S).sql
```

### 恢复数据库

```bash
# 从备份文件恢复
docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < ./backups/iot_platform_20250110_120000.sql

# 从本地备份恢复
docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < ~/iot_platform_backup_20250110_120000.sql
```

## 🔄 更新部署

### 更新代码

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新编译后端（如果修改了Java代码）
cd backend
mvn clean package -DskipTests

# 3. 重新构建前端（如果修改了前端代码）
cd frontend
npm run build

# 4. 停止旧容器
docker-compose down

# 5. 启动新容器
docker-compose up -d --build

# 6. 初始化数据库（如果数据库结构有变化）
docker exec iot-mysql mysql -uroot -proot123456 iot_platform < /docker-entrypoint-initdb.d/init.sql
```

### 更新Docker镜像

```bash
# 重新拉取最新镜像
docker-compose pull

# 重新构建并启动
docker-compose up -d --build --force-recreate
```

## 🔐 安全建议

### 生产环境部署

1. **修改默认密码**
   - 修改 `init.sql` 中的默认密码
   - 修改 `docker-compose.yml` 中的数据库密码
   - 修改 `docker-compose.yml` 中的Redis密码

2. **配置防火墙**
   ```bash
   # 只开放必要端口
   sudo ufw allow 22/tcp    # SSH
   sudo ufw allow 80/tcp    # HTTP
   sudo ufw allow 443/tcp   # HTTPS
   sudo ufw allow 3306/tcp  # MySQL（仅限内网）
   sudo ufw allow 6379/tcp  # Redis（仅限内网）
   sudo ufw allow 1883/tcp  # MQTT（仅限内网）
   ```

3. **启用SSL/TLS**
   - 为EMQX配置SSL证书
   - 使用HTTPS访问Dashboard
   - 使用MQTTS连接

4. **定期备份**
   - 设置定时任务，每天自动备份数据库
   - 保留最近30天的备份文件

5. **监控日志**
   - 定期查看容器日志
   - 设置日志轮转策略，避免日志文件过大

## 📞 技术支持

### 日志文件位置

| 服务 | 日志位置 |
|------|----------|
| MySQL | Docker容器日志，或挂载到 `emqx-log/mysql.log` |
| Redis | Docker容器日志 |
| EMQX | Docker容器日志，或挂载到 `emqx-log/emqx.log` |
| 后端 | Docker容器日志 |
| 前端 | Docker容器日志 |

### 查看实时日志

```bash
# 查看所有服务日志（实时）
docker-compose logs -f

# 查看特定服务日志（最后100行）
docker-compose logs --tail=100 mysql

# 持续查看日志
docker-compose logs -f mysql
```

## 📚 相关文档

- [快速开始指南](./快速开始指南.md)
- [API文档](./API文档.md)
- [用户手册](./用户手册.md)
- [故障排查](./故障排查.md)

## 🎉 部署检查清单

部署完成后，请检查以下项目：

- [ ] 所有容器都正常启动（`docker-compose ps`）
- [ ] 可以访问后端API（http://localhost:8080/api）
- [ ] 可以访问前端页面（http://localhost:8080）
- [ ] 可以访问EMQX Dashboard（http://localhost:18083）
- [ ] 数据库初始化成功
- [ ] 可以使用默认账号登录（admin/admin123456）
- [ ] 设备可以正常连接MQTT服务器
- [ ] 设备数据可以正常上报和查询

---

**部署成功！** 🎊

如有问题，请查看[故障排查](./故障排查.md)文档或查看容器日志。
