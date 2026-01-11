-- 设备日志表
CREATE TABLE `tb_device_log` (
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
