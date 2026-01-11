-- 数据库迁移脚本：创建消息通知表
-- 执行此脚本前，请先备份数据库！

USE iot_platform;

-- 检查表是否存在
SET @table_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tb_notification'
);

SET @sql = IF(
    @table_exists = 0,
    'CREATE TABLE `tb_notification` (
      `id` bigint NOT NULL AUTO_INCREMENT COMMENT ''通知ID'',
      `user_id` bigint NOT NULL COMMENT ''接收用户ID'',
      `alarm_id` bigint NOT NULL COMMENT ''关联报警ID'',
      `device_id` bigint NOT NULL COMMENT ''设备ID'',
      `device_code` varchar(50) NOT NULL COMMENT ''设备编码'',
      `device_name` varchar(100) NOT NULL COMMENT ''设备名称'',
      `alarm_level` varchar(20) NOT NULL COMMENT ''报警级别：critical/warning/info'',
      `alarm_message` text NOT NULL COMMENT ''报警消息'',
      `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否已读：0-未读，1-已读'',
      `read_time` datetime DEFAULT NULL COMMENT ''阅读时间'',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
      PRIMARY KEY (`id`),
      KEY `idx_user_id` (`user_id`),
      KEY `idx_alarm_id` (`alarm_id`),
      KEY `idx_is_read` (`is_read`),
      KEY `idx_user_read_time` (`user_id`, `is_read`, `create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''消息通知表'';',
    'SELECT ''Table tb_notification already exists'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration completed successfully!' AS result;
