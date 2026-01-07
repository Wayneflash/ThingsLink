# 数据库迁移说明

## 问题描述
数据库表 `tb_device` 缺少 `alarm_config` 和 `alarm_enabled` 字段，导致查询报错：
```
Unknown column 'alarm_config' in 'field list'
```

## 解决方案

### 方法1：使用安全迁移脚本（推荐）

执行 `migration_add_alarm_fields_safe.sql`，该脚本会检查字段是否存在，避免重复添加：

```bash
# 使用 MySQL 命令行
mysql -u root -p iot_platform < migration_add_alarm_fields_safe.sql

# 或者使用 MySQL Workbench、Navicat 等工具执行脚本
```

### 方法2：手动执行 SQL

如果方法1不可用，可以手动执行以下 SQL：

```sql
USE iot_platform;

-- 添加 alarm_config 字段
ALTER TABLE `tb_device` 
ADD COLUMN `alarm_config` text DEFAULT NULL COMMENT '告警配置(JSON格式): {level, conditions, notifyUsers, stackMode}' AFTER `offline_timeout`;

-- 添加 alarm_enabled 字段
ALTER TABLE `tb_device` 
ADD COLUMN `alarm_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '告警是否启用(0:禁用 1:启用)' AFTER `alarm_config`;

-- 添加索引
ALTER TABLE `tb_device` 
ADD INDEX `idx_alarm_enabled` (`alarm_enabled`);
```

**注意：** 如果字段已存在，会报错，但不会影响数据。可以忽略该错误。

### 方法3：使用 Docker（如果数据库在 Docker 中）

```bash
# 找到 MySQL 容器
docker ps | grep mysql

# 执行迁移脚本
docker exec -i <container_name> mysql -u root -p<password> iot_platform < migration_add_alarm_fields_safe.sql
```

## 验证

执行完成后，可以验证字段是否添加成功：

```sql
USE iot_platform;
DESCRIBE tb_device;
```

应该能看到 `alarm_config` 和 `alarm_enabled` 字段。

## 注意事项

1. **执行前请备份数据库**
2. 如果字段已存在，脚本会跳过，不会报错
3. 执行完成后，重启后端服务
