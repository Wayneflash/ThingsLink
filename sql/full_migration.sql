-- =====================================================
-- IoT平台完整数据库迁移脚本
-- 包含所有功能模块的数据库变更
-- 执行时间：2026-01-18
-- 说明：本脚本可重复执行（幂等设计），不会破坏现有数据
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 创建迁移历史表
-- =====================================================
CREATE TABLE IF NOT EXISTS `tb_migration_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `migration_file` VARCHAR(255) NOT NULL UNIQUE COMMENT '迁移文件名',
    `status` VARCHAR(50) DEFAULT 'success' COMMENT '执行状态',
    `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库迁移历史';

-- =====================================================
-- 2. 为 tb_alarm_log 表添加新字段
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

-- =====================================================
-- 3. 创建设备数据表（tb_device_data）
-- =====================================================

CREATE TABLE IF NOT EXISTS `tb_device_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(100) NOT NULL COMMENT '设备编码（冗余字段，便于查询）',
  `addr` varchar(50) NOT NULL COMMENT '属性标识符',
  `addrv` varchar(100) NOT NULL COMMENT '属性值',
  `ctime` datetime NOT NULL COMMENT '采集时间（设备上报的时间）',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间（服务器接收时间）',
  PRIMARY KEY (`id`, `ctime`),
  KEY `idx_device_time` (`device_id`, `ctime` DESC),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_addr_time` (`addr`, `ctime` DESC),
  KEY `idx_ctime` (`ctime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备数据表'
PARTITION BY RANGE (TO_DAYS(ctime)) (
  PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
  PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
  PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
  PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
  PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
  PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- 4. 创建视频设备表（tb_video_device）
-- =====================================================

CREATE TABLE IF NOT EXISTS `tb_video_device` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `device_id` VARCHAR(50) NOT NULL COMMENT 'GB28181设备编码',
  `channel_id` VARCHAR(50) NOT NULL COMMENT 'GB28181通道编码',
  `name` VARCHAR(100) NOT NULL COMMENT '视频设备名称',
  `group_id` BIGINT COMMENT '所属分组ID',
  `remark` VARCHAR(500) COMMENT '备注说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_device_channel` (`device_id`, `channel_id`) COMMENT '设备和通道唯一键',
  KEY `idx_group_id` (`group_id`) COMMENT '分组索引',
  KEY `idx_name` (`name`) COMMENT '名称索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频设备表';

-- 更新视频设备表字段长度（如果字段长度不是50则修改）
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_video_device' 
               AND COLUMN_NAME = 'device_id'
               AND CHARACTER_MAXIMUM_LENGTH = 50);
SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_video_device MODIFY COLUMN device_id VARCHAR(50) NOT NULL COMMENT ''GB28181设备编码''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'tb_video_device' 
               AND COLUMN_NAME = 'channel_id'
               AND CHARACTER_MAXIMUM_LENGTH = 50);
SET @sql := IF(@exist = 0, 
    'ALTER TABLE tb_video_device MODIFY COLUMN channel_id VARCHAR(50) NOT NULL COMMENT ''GB28181通道编码''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 5. 创建设备日志表（tb_device_log）
-- =====================================================

CREATE TABLE IF NOT EXISTS `tb_device_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(64) NOT NULL COMMENT '设备编码',
  `log_type` varchar(32) NOT NULL COMMENT '日志类型：online-设备上线, offline-设备离线, command-命令下发',
  `log_detail` varchar(255) DEFAULT NULL COMMENT '日志详情',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备日志表';

-- =====================================================
-- 6. 初始化WVP配置
-- =====================================================

-- 确保 system_config 表存在
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `description` VARCHAR(200) COMMENT '配置说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入WVP配置（如果已存在则更新）
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('wvp.server.url', 'https://lxs.fjqiaolong.com:18082', 'WVP server URL (HTTPS)'),
('wvp.server.username', 'wangyq', 'WVP username'),
('wvp.server.password', '4913dfad39dca93a85ba3be000abc3a3', 'WVP password (MD5)')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`);

-- =====================================================
-- 7. 创建预置产品（智能电表、智能水表、智能气表）及物模型
-- =====================================================

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

-- =====================================================
-- 8. 新增能源统计菜单
-- =====================================================

-- 新增能源统计菜单（一级菜单）
-- 先检查是否已存在，如果不存在则插入
SET @energy_menu_id = (SELECT id FROM `tb_menu` WHERE `menu_name` = '能源统计' AND `parent_id` = 0 LIMIT 1);

-- 如果不存在则插入
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
SELECT 0, '能源统计', 1, '/energy', NULL, 'energy:statistics', 'TrendCharts', 5, 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `menu_name` = '能源统计' AND `parent_id` = 0);

-- 获取能源统计菜单ID（插入后或已存在）
SET @energy_menu_id = (SELECT id FROM `tb_menu` WHERE `menu_name` = '能源统计' AND `parent_id` = 0 LIMIT 1);

-- 新增能耗统计子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
SELECT @energy_menu_id, '能耗统计', 2, '/energy/statistics', 'EnergyStatistics', 'energy:statistics:list', 'TrendCharts', 1, 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `menu_name` = '能耗统计' AND `parent_id` = @energy_menu_id);

-- 新增能耗趋势子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
SELECT @energy_menu_id, '能耗趋势', 2, '/energy/trend', 'EnergyTrend', 'energy:trend:list', 'DataAnalysis', 2, 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `menu_name` = '能耗趋势' AND `parent_id` = @energy_menu_id);

-- 新增能耗报表子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
SELECT @energy_menu_id, '能耗报表', 2, '/energy/report', 'EnergyReport', 'energy:report:list', 'Document', 3, 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `tb_menu` WHERE `menu_name` = '能耗报表' AND `parent_id` = @energy_menu_id);

-- 更新超级管理员角色的菜单权限（包含能源统计菜单）
UPDATE `tb_role` 
SET `menu_ids` = CASE 
    WHEN `menu_ids` IS NULL OR `menu_ids` = '' THEN 'energy'
    WHEN `menu_ids` NOT LIKE '%energy%' THEN CONCAT(`menu_ids`, ',energy')
    ELSE `menu_ids`
END
WHERE `role_code` = 'super_admin';

-- =====================================================
-- 记录所有迁移历史
-- =====================================================

INSERT INTO tb_migration_history (migration_file, status, executed_at) VALUES
('001_create_migration_history.sql', 'success', NOW()),
('002_add_alarm_log_fields.sql', 'success', NOW()),
('003_create_device_data_table.sql', 'success', NOW()),
('003_create_video_device_table.sql', 'success', NOW()),
('004_create_device_log_table.sql', 'success', NOW()),
('004_init_wvp_config.sql', 'success', NOW()),
('005_update_video_device_fields.sql', 'success', NOW()),
('001_create_preset_products.sql', 'success', NOW()),
('002_add_energy_statistics_menu.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 迁移完成提示
-- =====================================================
SELECT '数据库迁移完成！' AS message;
SELECT COUNT(*) AS total_migrations FROM tb_migration_history;
