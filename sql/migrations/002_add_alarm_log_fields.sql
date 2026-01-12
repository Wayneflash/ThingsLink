-- =====================================================
-- 迁移脚本: 002_add_alarm_log_fields.sql
-- 描述: 为 tb_alarm_log 表添加新字段
-- 创建时间: 2026-01-12
-- =====================================================

-- 添加 trigger_operator 字段（触发时的运算符）
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_alarm_log' 
               AND COLUMN_NAME = 'trigger_operator');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_alarm_log ADD COLUMN trigger_operator VARCHAR(50) DEFAULT NULL COMMENT ''触发时的运算符'' AFTER metric', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 trigger_threshold 字段（触发时的阈值）
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_alarm_log' 
               AND COLUMN_NAME = 'trigger_threshold');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_alarm_log ADD COLUMN trigger_threshold DOUBLE DEFAULT NULL COMMENT ''触发时的阈值'' AFTER trigger_operator', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 notify_users 字段（通知人员ID列表）
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_alarm_log' 
               AND COLUMN_NAME = 'notify_users');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_alarm_log ADD COLUMN notify_users TEXT DEFAULT NULL COMMENT ''通知人员ID列表JSON'' AFTER trigger_threshold', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 记录迁移历史
INSERT IGNORE INTO tb_migration_history (migration_file, status) 
VALUES ('002_add_alarm_log_fields.sql', 'completed');
