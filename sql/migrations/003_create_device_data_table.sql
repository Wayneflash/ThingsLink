-- ========================================
-- 创建设备数据表（tb_device_data）
-- 用于存储设备上报的实时数据
-- 日期：2026-01-13
-- ========================================

-- 创建设备数据表（如果不存在）
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

-- 记录迁移历史
INSERT INTO tb_migration_history (migration_file, status, executed_at) 
VALUES ('003_create_device_data_table.sql', 'success', NOW())
ON DUPLICATE KEY UPDATE status = 'success', executed_at = NOW();
