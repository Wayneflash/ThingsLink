-- 为设备表添加告警配置字段
ALTER TABLE tb_device 
ADD COLUMN alarm_config TEXT COMMENT '告警配置(JSON格式)' AFTER offline_timeout,
ADD COLUMN alarm_enabled TINYINT(1) DEFAULT 0 COMMENT '告警是否启用(0:禁用 1:启用)' AFTER alarm_config;

-- 更新注释
ALTER TABLE tb_device MODIFY COLUMN alarm_config TEXT COMMENT '告警配置(JSON格式): {level, conditions, notifyUsers, stackMode}';

-- 创建索引以提升查询性能
CREATE INDEX idx_alarm_enabled ON tb_device(alarm_enabled);
