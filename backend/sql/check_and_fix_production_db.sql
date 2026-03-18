-- 检查并修复生产环境数据库表结构
-- 使用方法: mysql -h 117.72.222.8 -P 3306 -u root -proot123456 thingslink_1_0 < check_and_fix_production_db.sql

-- 1. 检查 tb_scene_log 表结构
SELECT '=== 检查 tb_scene_log 表结构 ===' AS info;
DESC tb_scene_log;

-- 2. 检查 tb_scene_rule 表结构
SELECT '=== 检查 tb_scene_rule 表结构 ===' AS info;
DESC tb_scene_rule;

-- 3. 检查 tb_scene_log 表是否有 create_time 字段
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'thingslink_1_0'
  AND TABLE_NAME = 'tb_scene_log'
  AND COLUMN_NAME = 'create_time';

-- 4. 检查 tb_scene_log 表是否有 target_value 字段
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'thingslink_1_0'
  AND TABLE_NAME = 'tb_scene_log'
  AND COLUMN_NAME = 'target_value';

-- 5. 检查 tb_scene_rule 表是否有 target_value 字段
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'thingslink_1_0'
  AND TABLE_NAME = 'tb_scene_rule'
  AND COLUMN_NAME = 'target_value';

-- 6. 检查最近的日志记录，看 target_value 是否有值
SELECT 
    id,
    rule_id,
    rule_name,
    target_value,
    execute_result,
    create_time
FROM tb_scene_log
ORDER BY create_time DESC
LIMIT 10;

-- 7. 检查规则记录，看 target_value 是否有值
SELECT 
    id,
    rule_name,
    target_attr,
    target_value,
    enabled,
    create_time
FROM tb_scene_rule
ORDER BY create_time DESC
LIMIT 10;
