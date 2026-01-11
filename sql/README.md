# 数据库SQL脚本说明

## 概述

本目录包含所有数据库表的创建和修改SQL脚本。

## 使用说明

### 执行顺序

SQL文件按以下顺序执行（按文件名排序）：

1. `fix_system_config_table.sql` - 修复系统配置表
2. `migration_add_alarm_fields_safe.sql` - 添加报警配置字段
3. `migration_add_alarm_trigger_fields.sql` - 添加报警触发字段
4. `migration_add_notification_table.sql` - 添加通知表
5. `create_notification_table_simple.sql` - 简化版通知表
6. `add_device_log_table.sql` - 添加设备日志表

### 执行方式

**方式一：使用MySQL客户端直接执行**
```bash
mysql -u root -p iot_platform < sql/xxx.sql
```

**方式二：使用备份恢复脚本**
```bash
# Windows
restore-database.bat

# Linux/Mac
bash restore-database.sh
```

## SQL文件说明

| 文件名 | 说明 | 执行时机 |
|---------|------|---------|
| fix_system_config_table.sql | 修复系统配置表结构 | 首次部署 |
| migration_add_alarm_fields_safe.sql | 添加报警配置字段到设备表 | 首次部署 |
| migration_add_alarm_trigger_fields.sql | 添加报警触发字段 | 首次部署 |
| migration_add_notification_table.sql | 添加通知表 | 首次部署 |
| create_notification_table_simple.sql | 简化版通知表（推荐使用） | 首次部署 |
| add_device_log_table.sql | 添加设备日志表 | 首次部署 |

## 重要提示

1. **备份优先**：执行任何SQL脚本前，务必备份数据库
2. **幂等性**：所有SQL脚本都支持重复执行（使用 `IF NOT EXISTS` 和 `IF EXISTS`）
3. **数据安全**：SQL脚本不会删除已有数据，只会添加新表/字段
4. **字符集**：统一使用 `utf8mb4` 字符集
5. **引擎**：统一使用 `InnoDB` 引擎

## 数据备份

### 自动备份

项目包含自动备份脚本：
- Windows: `backup-database.bat`
- Linux/Mac: `backup-database.sh`

### 手动备份

```bash
mysqldump -u root -p iot_platform > backup_iot_platform_YYYYMMDD_HHMMSS.sql
```

## 故障恢复

如果执行SQL后出现问题：

1. 恢复最近备份
2. 检查错误日志
3. 重新执行SQL脚本

## 版本历史

| 日期 | 变更内容 |
|------|---------|
| 2026-01-11 | 添加设备日志表、离线报警配置、优化SQL文件组织 |
