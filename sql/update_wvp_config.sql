-- =====================================================
-- 更新WVP配置
-- 文件名：update_wvp_config.sql
-- 创建时间：2026-01-16
-- 说明：更新WVP服务器账号密码配置
-- =====================================================

-- 更新WVP服务器URL（如果不存在则插入）
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES ('wvp.server.url', 'https://lxs.fjqiaolong.com:18082', 'WVP server URL (HTTPS)')
ON DUPLICATE KEY UPDATE `config_value` = 'https://lxs.fjqiaolong.com:18082', `description` = 'WVP server URL (HTTPS)';

-- 更新WVP用户名
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES ('wvp.server.username', 'wangyq', 'WVP username')
ON DUPLICATE KEY UPDATE `config_value` = 'wangyq', `description` = 'WVP username';

-- 更新WVP密码（MD5加密后的值）
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES ('wvp.server.password', '4913dfad39dca93a85ba3be000abc3a3', 'WVP password (MD5)')
ON DUPLICATE KEY UPDATE `config_value` = '4913dfad39dca93a85ba3be000abc3a3', `description` = 'WVP password (MD5)';

-- 显示更新后的配置
SELECT 'WVP配置更新完成' AS message;
SELECT config_key, config_value, description 
FROM system_config 
WHERE config_key LIKE 'wvp%'
ORDER BY config_key;
