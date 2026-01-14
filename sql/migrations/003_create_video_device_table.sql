-- =====================================================
-- 迁移脚本：创建视频设备表
-- 文件名：003_create_video_device_table.sql
-- 创建时间：2026-01-14
-- 说明：创建GB28181视频设备管理表
-- =====================================================

-- 创建视频设备表
CREATE TABLE IF NOT EXISTS `tb_video_device` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `device_id` VARCHAR(20) NOT NULL COMMENT 'GB28181设备编码（20位）',
  `channel_id` VARCHAR(20) NOT NULL COMMENT 'GB28181通道编码（20位）',
  `name` VARCHAR(100) NOT NULL COMMENT '视频设备名称',
  `group_id` BIGINT COMMENT '所属分组ID',
  `remark` VARCHAR(500) COMMENT '备注说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_device_channel` (`device_id`, `channel_id`) COMMENT '设备和通道唯一键',
  KEY `idx_group_id` (`group_id`) COMMENT '分组索引',
  KEY `idx_name` (`name`) COMMENT '名称索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频设备表';

-- 记录迁移历史
INSERT INTO `tb_migration_history` (`migration_file`, `status`, `executed_at`) 
VALUES ('003_create_video_device_table.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE `status` = 'success', `executed_at` = NOW();

-- 提示信息
SELECT 'tb_video_device 表创建完成' AS message;
SELECT COUNT(*) AS total_count FROM `tb_video_device`;
