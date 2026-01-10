-- 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认MQTT配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('platform.mqtt.broker', 'localhost', '平台MQTT服务器地址'),
('platform.mqtt.port', '1883', '平台MQTT服务器端口'),
('platform.mqtt.username', 'admin', '平台MQTT连接用户名'),
('platform.mqtt.password', 'admin123.', '平台MQTT连接密码')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
