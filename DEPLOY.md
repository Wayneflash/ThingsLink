# IOT 平台部署指南

## 快速部署

### 1. 检查服务器磁盘情况

**有独立硬盘：**
```bash
# 编辑 .env 文件
nano .env

# 修改数据路径
DATA_PATH=/data/iot        # Linux
# 或
DATA_PATH=D:/IOTData       # Windows
```

**只有系统盘：**
```bash
# 编辑 .env 文件
nano .env

# 注释掉或留空
# DATA_PATH=
```

### 2. 启动服务

```bash
docker-compose up -d
```

## 存储路径说明

| 配置 | 数据存储位置 | 适用场景 |
|------|-------------|---------|
| `DATA_PATH=/data/iot` | `/data/iot/mysql`<br>`/data/iot/emqx` | 有独立数据盘 |
| `DATA_PATH=D:/IOTData` | `D:/IOTData/mysql`<br>`D:/IOTData/emqx` | Windows 有D盘 |
| 不设置（默认） | `./data/mysql`<br>`./data/emqx` | 只有系统盘 |

## 存储空间预估

- **1万张报警图片**：约 5GB
- **数据库（无图片）**：约 500MB
- **EMQX 数据**：约 100MB

**建议预留空间：至少 20GB**
