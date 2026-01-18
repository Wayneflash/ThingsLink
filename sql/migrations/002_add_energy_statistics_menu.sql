-- 能源管理MVP - 新增能源统计菜单
-- 功能：新增能源统计一级菜单及其子菜单（能耗统计、能耗趋势、能耗报表）
-- 创建时间：2026-01-18
-- 作者：AI Assistant

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 新增能源统计菜单（一级菜单）
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
VALUES (0, '能源统计', 1, '/energy', NULL, 'energy:statistics', 'TrendCharts', 5, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 获取能源统计菜单ID
SET @energy_menu_id = LAST_INSERT_ID();

-- 新增能耗统计子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
VALUES (@energy_menu_id, '能耗统计', 2, '/energy/statistics', 'EnergyStatistics', 'energy:statistics:list', 'DataLine', 1, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 新增能耗趋势子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
VALUES (@energy_menu_id, '能耗趋势', 2, '/energy/trend', 'EnergyTrend', 'energy:trend:list', 'DataAnalysis', 2, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 新增能耗报表子菜单
INSERT INTO `tb_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_time`) 
VALUES (@energy_menu_id, '能耗报表', 2, '/energy/report', 'EnergyReport', 'energy:report:list', 'Document', 3, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 更新超级管理员角色的菜单权限（包含能源统计菜单）
-- 注意：menu_ids 字段是逗号分隔的字符串格式，不是 JSON
-- 如果 menu_ids 不包含 'energy'，则追加到末尾
UPDATE `tb_role` 
SET `menu_ids` = CASE 
    WHEN `menu_ids` IS NULL OR `menu_ids` = '' THEN 'energy'
    WHEN `menu_ids` NOT LIKE '%energy%' THEN CONCAT(`menu_ids`, ',energy')
    ELSE `menu_ids`
END
WHERE `role_code` = 'super_admin';

-- 记录迁移历史
INSERT INTO tb_migration_history (migration_file, status, executed_at) 
VALUES ('002_add_energy_statistics_menu.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();

SET FOREIGN_KEY_CHECKS = 1;
