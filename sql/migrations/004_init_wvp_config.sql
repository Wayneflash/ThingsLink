-- =====================================================
-- 迁移脚本：初始化WVP配置
-- 文件名：004_init_wvp_config.sql
-- 创建时间：2026-01-14
-- 说明：初始化WVP服务器配置到 system_config 表
-- =====================================================

-- 确保 system_config 表存在（注意字段名是 config_key 和 config_value）
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `description` VARCHAR(200) COMMENT '配置说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入WVP配置（如果已存在则更新配置值）
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('wvp.server.url', 'https://lxs.fjqiaolong.com:18082', 'WVP server URL (HTTPS)'),
('wvp.server.username', 'wangyq', 'WVP username'),
('wvp.server.password', '4913dfad39dca93a85ba3be000abc3a3', 'WVP password (MD5)')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`);

-- 记录迁移历史
INSERT INTO `tb_migration_history` (`migration_file`, `status`, `executed_at`) 
VALUES ('004_init_wvp_config.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE `status` = 'success', `executed_at` = NOW();

-- 提示信息
SELECT 'WVP配置初始化完成' AS message;
SELECT * FROM `system_config` WHERE `config_key` LIKE 'wvp.%';
