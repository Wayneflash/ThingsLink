-- 创建迁移历史表（记录已执行的迁移脚本）
CREATE TABLE IF NOT EXISTS `tb_migration_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `migration_file` VARCHAR(255) NOT NULL UNIQUE COMMENT '迁移文件名',
    `status` VARCHAR(50) DEFAULT 'success' COMMENT '执行状态',
    `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库迁移历史';
