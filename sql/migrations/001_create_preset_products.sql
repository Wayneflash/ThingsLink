-- 能源管理MVP - 预置产品及物模型
-- 功能：创建智能电表、智能水表、智能气表三个预置产品及其物模型
-- 创建时间：2026-01-18
-- 作者：AI Assistant

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 预置产品：智能电表
INSERT INTO `tb_product` (`product_name`, `product_model`, `protocol`, `description`, `status`, `create_time`, `update_time`) 
VALUES ('智能电表', 'ELEC', 'MQTT', '用于监测电力能耗', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `product_name` = VALUES(`product_name`), `update_time` = NOW();

-- 获取电表产品ID（兼容ON DUPLICATE KEY UPDATE的情况）
SET @elec_product_id = (SELECT id FROM `tb_product` WHERE `product_model` = 'ELEC' LIMIT 1);

-- 电表物模型属性
INSERT INTO `tb_attribute` (`product_id`, `addr`, `attr_name`, `data_type`, `unit`, `description`, `create_time`) VALUES
(@elec_product_id, 'voltage', '电压', 'float', 'V', '实时电压', NOW()),
(@elec_product_id, 'current', '电流', 'float', 'A', '实时电流', NOW()),
(@elec_product_id, 'active_power', '有功功率', 'float', 'kW', '实时有功功率', NOW()),
(@elec_product_id, 'reactive_power', '无功功率', 'float', 'kvar', '实时无功功率', NOW()),
(@elec_product_id, 'power_factor', '功率因数', 'float', '', '功率因数（0-1）', NOW()),
(@elec_product_id, 'total_energy', '总有功电能', 'float', 'kWh', '表底示数（累计值）', NOW())
ON DUPLICATE KEY UPDATE `attr_name` = VALUES(`attr_name`);

-- 预置产品：智能水表
INSERT INTO `tb_product` (`product_name`, `product_model`, `protocol`, `description`, `status`, `create_time`, `update_time`) 
VALUES ('智能水表', 'WATER', 'MQTT', '用于监测水耗', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `product_name` = VALUES(`product_name`), `update_time` = NOW();

-- 获取水表产品ID（兼容ON DUPLICATE KEY UPDATE的情况）
SET @water_product_id = (SELECT id FROM `tb_product` WHERE `product_model` = 'WATER' LIMIT 1);

-- 水表物模型属性
INSERT INTO `tb_attribute` (`product_id`, `addr`, `attr_name`, `data_type`, `unit`, `description`, `create_time`) VALUES
(@water_product_id, 'total_flow', '总流量', 'float', 'm³', '表底示数（累计值）', NOW()),
(@water_product_id, 'instantaneous_flow', '瞬时流量', 'float', 'm³/h', '实时流量', NOW()),
(@water_product_id, 'water_pressure', '水压', 'float', 'MPa', '实时水压', NOW()),
(@water_product_id, 'valve_status', '阀门状态', 'int', '', '0-关闭，1-打开', NOW())
ON DUPLICATE KEY UPDATE `attr_name` = VALUES(`attr_name`);

-- 预置产品：智能气表
INSERT INTO `tb_product` (`product_name`, `product_model`, `protocol`, `description`, `status`, `create_time`, `update_time`) 
VALUES ('智能气表', 'GAS', 'MQTT', '用于监测气耗', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `product_name` = VALUES(`product_name`), `update_time` = NOW();

-- 获取气表产品ID（兼容ON DUPLICATE KEY UPDATE的情况）
SET @gas_product_id = (SELECT id FROM `tb_product` WHERE `product_model` = 'GAS' LIMIT 1);

-- 气表物模型属性
INSERT INTO `tb_attribute` (`product_id`, `addr`, `attr_name`, `data_type`, `unit`, `description`, `create_time`) VALUES
(@gas_product_id, 'total_gas', '总用气量', 'float', 'm³', '表底示数（累计值）', NOW()),
(@gas_product_id, 'instantaneous_flow', '瞬时流量', 'float', 'm³/h', '实时流量', NOW()),
(@gas_product_id, 'gas_pressure', '气压', 'float', 'kPa', '实时气压', NOW()),
(@gas_product_id, 'gas_temperature', '气体温度', 'float', '℃', '实时温度', NOW())
ON DUPLICATE KEY UPDATE `attr_name` = VALUES(`attr_name`);

-- 记录迁移历史
INSERT INTO tb_migration_history (migration_file, status, executed_at) 
VALUES ('001_create_preset_products.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();

SET FOREIGN_KEY_CHECKS = 1;
