-- 数据库迁移脚本：为设备表添加告警配置字段（安全版本，检查字段是否存在）
-- 执行此脚本前，请先备份数据库！

USE iot_platform;

-- 检查并添加 alarm_config 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `tb_device` ADD COLUMN `alarm_config` text DEFAULT NULL COMMENT ''告警配置(JSON格式): {level, conditions, notifyUsers, stackMode}'' AFTER `offline_timeout`;',
        'SELECT ''Column alarm_config already exists'' AS message;'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_device'
    AND COLUMN_NAME = 'alarm_config'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 alarm_enabled 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `tb_device` ADD COLUMN `alarm_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''告警是否启用(0:禁用 1:启用)'' AFTER `alarm_config`;',
        'SELECT ''Column alarm_enabled already exists'' AS message;'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_device'
    AND COLUMN_NAME = 'alarm_enabled'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `tb_device` ADD INDEX `idx_alarm_enabled` (`alarm_enabled`);',
        'SELECT ''Index idx_alarm_enabled already exists'' AS message;'
    )
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_device'
    AND INDEX_NAME = 'idx_alarm_enabled'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration completed successfully!' AS result;
