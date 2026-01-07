-- 数据库迁移脚本：为设备表添加告警配置字段
-- 执行此脚本前，请先备份数据库！

USE iot_platform;

-- 添加 alarm_config 字段（如果不存在）
ALTER TABLE `tb_device` 
ADD COLUMN IF NOT EXISTS `alarm_config` text DEFAULT NULL COMMENT '告警配置(JSON格式): {level, conditions, notifyUsers, stackMode}' AFTER `offline_timeout`;

-- 添加 alarm_enabled 字段（如果不存在）
ALTER TABLE `tb_device` 
ADD COLUMN IF NOT EXISTS `alarm_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '告警是否启用(0:禁用 1:启用)' AFTER `alarm_config`;

-- 添加索引（如果不存在）
ALTER TABLE `tb_device` 
ADD INDEX IF NOT EXISTS `idx_alarm_enabled` (`alarm_enabled`);

SELECT 'Migration completed successfully!' AS result;
