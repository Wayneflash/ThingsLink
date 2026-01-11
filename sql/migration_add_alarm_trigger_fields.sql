-- 数据库迁移脚本：为告警日志表添加触发配置字段（用于恢复判断）
-- 执行此脚本前，请先备份数据库！

USE iot_platform;

-- 检查并添加 trigger_operator 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `tb_alarm_log` ADD COLUMN `trigger_operator` varchar(10) DEFAULT NULL COMMENT ''触发时的运算符（用于恢复判断）'' AFTER `metric`;',
        'SELECT ''Column trigger_operator already exists'' AS message;'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_alarm_log'
    AND COLUMN_NAME = 'trigger_operator'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 trigger_threshold 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `tb_alarm_log` ADD COLUMN `trigger_threshold` double DEFAULT NULL COMMENT ''触发时的阈值（用于恢复判断）'' AFTER `trigger_operator`;',
        'SELECT ''Column trigger_threshold already exists'' AS message;'
    )
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_alarm_log'
    AND COLUMN_NAME = 'trigger_threshold'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration completed successfully!' AS result;
