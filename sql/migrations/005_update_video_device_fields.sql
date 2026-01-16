-- =====================================================
-- 迁移脚本：更新视频设备表字段长度
-- 文件名：005_update_video_device_fields.sql
-- 创建时间：2026-01-14
-- 说明：将device_id和channel_id字段从VARCHAR(20)更新为VARCHAR(50)
-- =====================================================

-- 检查device_id字段长度，如果不是50则修改
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_video_device' 
               AND COLUMN_NAME = 'device_id'
               AND CHARACTER_MAXIMUM_LENGTH = 50);

SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_video_device MODIFY COLUMN device_id VARCHAR(50) NOT NULL COMMENT ''GB28181设备编码''', 
    'SELECT ''device_id字段长度已经是50，无需修改'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查channel_id字段长度，如果不是50则修改
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_video_device' 
               AND COLUMN_NAME = 'channel_id'
               AND CHARACTER_MAXIMUM_LENGTH = 50);

SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_video_device MODIFY COLUMN channel_id VARCHAR(50) NOT NULL COMMENT ''GB28181通道编码''', 
    'SELECT ''channel_id字段长度已经是50，无需修改'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 记录迁移历史（兼容不同表结构）
SET @has_migration_name := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                            WHERE TABLE_SCHEMA = DATABASE() 
                            AND TABLE_NAME = 'tb_migration_history' 
                            AND COLUMN_NAME = 'migration_name');

SET @sql := IF(@has_migration_name > 0,
    'INSERT INTO tb_migration_history (migration_name, migration_file, status, executed_at) VALUES (\"更新视频设备表字段长度\", \"005_update_video_device_fields.sql\", \"success\", NOW()) ON DUPLICATE KEY UPDATE status = \"success\", executed_at = NOW()',
    'INSERT INTO tb_migration_history (migration_file, status, executed_at) VALUES (\"005_update_video_device_fields.sql\", \"success\", NOW()) ON DUPLICATE KEY UPDATE status = \"success\", executed_at = NOW()');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 提示信息
SELECT 'tb_video_device表字段更新完成' AS message;
SELECT COLUMN_NAME, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'tb_video_device' 
AND COLUMN_NAME IN ('device_id', 'channel_id');
