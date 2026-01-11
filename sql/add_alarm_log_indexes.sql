-- 为报警日志表添加索引，优化报警分析查询性能
-- 执行时间: 2026-01-11

-- 1. 添加设备编码索引（用于按设备查询）
CREATE INDEX idx_alarm_log_device_code ON tb_alarm_log(device_code);

-- 2. 添加触发时间索引（用于时间范围查询）
CREATE INDEX idx_alarm_log_trigger_time ON tb_alarm_log(trigger_time);

-- 3. 添加报警级别索引（用于按级别筛选）
CREATE INDEX idx_alarm_log_alarm_level ON tb_alarm_log(alarm_level);

-- 4. 添加处理状态索引（用于按状态筛选）
CREATE INDEX idx_alarm_log_status ON tb_alarm_log(status);

-- 5. 添加组合索引（设备编码+触发时间，用于报警分析查询）
CREATE INDEX idx_alarm_log_device_time ON tb_alarm_log(device_code, trigger_time);

-- 6. 添加组合索引（设备编码+处理状态，用于未处理报警查询）
CREATE INDEX idx_alarm_log_device_status ON tb_alarm_log(device_code, status);
