-- 远程开关（4路继电器）预置产品
-- 产品型号 RELAY-4CH，协议 RELAY，4 属性 + 8 命令
-- 创建时间：2026-02-11

SET NAMES utf8mb4;

-- 预置产品：远程开关
INSERT INTO `tb_product` (`product_name`, `product_model`, `protocol`, `description`, `status`, `create_time`, `update_time`)
VALUES ('远程开关', 'RELAY-4CH', 'RELAY', '4路远程继电器，支持状态上报与开关控制', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `product_name` = VALUES(`product_name`), `protocol` = VALUES(`protocol`), `update_time` = NOW();

SET @relay_product_id = (SELECT id FROM `tb_product` WHERE `product_model` = 'RELAY-4CH' LIMIT 1);

-- 远程开关属性（4 个，固化）
INSERT INTO `tb_attribute` (`product_id`, `addr`, `attr_name`, `data_type`, `unit`, `description`, `create_time`) VALUES
(@relay_product_id, 'switch1', '开关1', 'int', NULL, '通道1状态：0-关，1-开', NOW()),
(@relay_product_id, 'switch2', '开关2', 'int', NULL, '通道2状态：0-关，1-开', NOW()),
(@relay_product_id, 'switch3', '开关3', 'int', NULL, '通道3状态：0-关，1-开', NOW()),
(@relay_product_id, 'switch4', '开关4', 'int', NULL, '通道4状态：0-关，1-开', NOW())
ON DUPLICATE KEY UPDATE `attr_name` = VALUES(`attr_name`);

-- 远程开关命令（8 个，固化）
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch1', '开关1开', '1', '打开通道1', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch1' AND command_value = '1');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch1', '开关1关', '0', '关闭通道1', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch1' AND command_value = '0');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch2', '开关2开', '1', '打开通道2', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch2' AND command_value = '1');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch2', '开关2关', '0', '关闭通道2', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch2' AND command_value = '0');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch3', '开关3开', '1', '打开通道3', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch3' AND command_value = '1');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch3', '开关3关', '0', '关闭通道3', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch3' AND command_value = '0');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch4', '开关4开', '1', '打开通道4', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch4' AND command_value = '1');
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`, `create_time`)
SELECT @relay_product_id, 'switch4', '开关4关', '0', '关闭通道4', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tb_command` WHERE product_id = @relay_product_id AND addr = 'switch4' AND command_value = '0');

-- 记录迁移历史
INSERT INTO tb_migration_history (migration_file, status, executed_at)
VALUES ('007_create_relay_product.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();
