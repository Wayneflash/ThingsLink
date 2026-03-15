-- 场景联动规则表
CREATE TABLE IF NOT EXISTS tb_scene_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    
    -- 触发条件
    trigger_device_id BIGINT NOT NULL COMMENT '触发设备ID',
    trigger_attr VARCHAR(50) NOT NULL COMMENT '触发属性地址（addr）',
    operator VARCHAR(20) NOT NULL COMMENT '操作符：gt-大于, lt-小于, gte-大于等于, lte-小于等于, eq-等于, neq-不等于',
    trigger_value VARCHAR(100) NOT NULL COMMENT '触发阈值',
    
    -- 执行动作
    target_device_id BIGINT NOT NULL COMMENT '目标设备ID',
    target_attr VARCHAR(50) NOT NULL COMMENT '目标属性地址（addr）',
    target_value VARCHAR(100) NOT NULL COMMENT '下发值',
    
    -- 统计信息
    trigger_count INT DEFAULT 0 COMMENT '触发次数',
    last_triggered_at DATETIME COMMENT '上次触发时间',
    
    -- 基础字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_trigger_device (trigger_device_id),
    INDEX idx_target_device (target_device_id),
    INDEX idx_enabled (enabled),
    
    -- 设备唯一约束：一个设备只能出现在一个规则中（无论是触发还是目标）
    UNIQUE INDEX uk_trigger_device (trigger_device_id),
    UNIQUE INDEX uk_target_device (target_device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景联动规则表';

-- 场景联动执行日志表
CREATE TABLE IF NOT EXISTS tb_scene_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称（冗余存储）',
    
    -- 触发信息
    trigger_device_id BIGINT NOT NULL COMMENT '触发设备ID',
    trigger_device_name VARCHAR(100) COMMENT '触发设备名称',
    trigger_attr VARCHAR(50) NOT NULL COMMENT '触发属性',
    trigger_value VARCHAR(100) NOT NULL COMMENT '实际触发值',
    
    -- 执行信息
    target_device_id BIGINT NOT NULL COMMENT '目标设备ID',
    target_device_name VARCHAR(100) COMMENT '目标设备名称',
    target_attr VARCHAR(50) NOT NULL COMMENT '目标属性',
    target_value VARCHAR(100) NOT NULL COMMENT '下发值',
    
    -- 执行结果
    execute_result VARCHAR(20) DEFAULT 'success' COMMENT '执行结果：success-成功, failed-失败',
    error_message VARCHAR(500) COMMENT '错误信息',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    
    INDEX idx_rule_id (rule_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景联动执行日志表';
