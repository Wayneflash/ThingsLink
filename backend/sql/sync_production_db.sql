-- 同步生产环境数据库表结构
-- 场景联动相关表结构更新

-- 使用方法: mysql -h 127.0.0.1 -P 3306 -u root -proot123456 thingslink_1_0 < sync_production_db.sql

-- 1. 确保 tb_scene_log 表有 create_time 字段
ALTER TABLE tb_scene_log 
ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间';

-- 2. 确保 tb_scene_log 表的 target_value 字段正确
ALTER TABLE tb_scene_log 
MODIFY COLUMN target_value VARCHAR(100) NOT NULL COMMENT '下发值';

-- 3. 确保 tb_scene_rule 表的 target_value 字段正确
ALTER TABLE tb_scene_rule 
MODIFY COLUMN target_value VARCHAR(100) NOT NULL COMMENT '下发值';

-- 4. 确保 tb_scene_log 表的索引正确
ALTER TABLE tb_scene_log 
ADD INDEX IF NOT EXISTS idx_rule_id (rule_id);

ALTER TABLE tb_scene_log 
ADD INDEX IF NOT EXISTS idx_create_time (create_time);

-- 5. 确保 tb_scene_rule 表的索引正确
ALTER TABLE tb_scene_rule 
ADD INDEX IF NOT EXISTS idx_trigger_device (trigger_device_id);

ALTER TABLE tb_scene_rule 
ADD INDEX IF NOT EXISTS idx_target_device (target_device_id);

ALTER TABLE tb_scene_rule 
ADD INDEX IF NOT EXISTS idx_enabled (enabled);

-- 6. 确保 tb_scene_rule 表的唯一约束正确
ALTER TABLE tb_scene_rule 
ADD UNIQUE INDEX IF NOT EXISTS uk_trigger_device (trigger_device_id);

ALTER TABLE tb_scene_rule 
ADD UNIQUE INDEX IF NOT EXISTS uk_target_device (target_device_id);

-- 7. 验证表结构
SELECT '=== tb_scene_log 表结构 ===' AS info;
DESC tb_scene_log;

SELECT '=== tb_scene_rule 表结构 ===' AS info;
DESC tb_scene_rule;

SELECT '=== 同步完成 ===' AS info;
