-- 创建告警日志表
USE iot_platform;

DROP TABLE IF EXISTS `tb_alarm_log`;
CREATE TABLE `tb_alarm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编码',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `alarm_level` varchar(20) NOT NULL COMMENT '告警级别：critical/warning/info',
  `alarm_message` text NOT NULL COMMENT '告警消息（描述触发的条件和实际值）',
  `trigger_time` datetime NOT NULL COMMENT '触发时间',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处理状态：0-未处理，1-已处理',
  `handler` varchar(50) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_alarm_level` (`alarm_level`),
  KEY `idx_trigger_time` (`trigger_time`),
  KEY `idx_status` (`status`),
  KEY `idx_device_status_time` (`device_id`, `status`, `trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警日志表';

SELECT '告警日志表创建成功！' AS result;
