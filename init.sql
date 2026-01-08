-- IoT Platform Database Initialization
-- 数据库：iot_platform (含用户权限 + 设备分组)

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: 用户表
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(200) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `group_id` bigint DEFAULT 0 COMMENT '用户分组ID',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `role_id` bigint NOT NULL COMMENT '角色ID（单选，必填）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table: 角色表
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `menu_ids` text DEFAULT NULL COMMENT '菜单权限ID列表（JSON数组）',
  `data_scope` tinyint NOT NULL DEFAULT 2 COMMENT '数据权限：0-全部，1-本部门，2-仅本人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ----------------------------
-- Table: 菜单表
-- ----------------------------
DROP TABLE IF EXISTS `tb_menu`;
CREATE TABLE `tb_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID（0为顶级）',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- ----------------------------
-- Table: 设备分组表 ⭐️ 新增
-- ----------------------------
DROP TABLE IF EXISTS `tb_device_group`;
CREATE TABLE `tb_device_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分组ID（0为顶级）',
  `group_name` varchar(100) NOT NULL COMMENT '分组名称',
  `group_type` varchar(50) DEFAULT 'default' COMMENT '分组类型（地域/业务/楼层）',
  `description` varchar(500) DEFAULT NULL COMMENT '分组描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分组表';

-- ----------------------------
-- Table: 产品表
-- ----------------------------
DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_model` varchar(100) NOT NULL COMMENT '产品型号',
  `protocol` varchar(50) NOT NULL DEFAULT 'MQTT' COMMENT '协议类型',
  `description` varchar(500) DEFAULT NULL COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_model` (`product_model`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- ----------------------------
-- Table: 产品属性表
-- ----------------------------
DROP TABLE IF EXISTS `tb_attribute`;
CREATE TABLE `tb_attribute` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '属性ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `addr` varchar(50) NOT NULL COMMENT '属性标识符（如：tem, hum）',
  `attr_name` varchar(100) NOT NULL COMMENT '属性名称',
  `data_type` varchar(20) NOT NULL DEFAULT 'string' COMMENT '数据类型：string, int, float, bool',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位（如：℃, %）',
  `description` varchar(200) DEFAULT NULL COMMENT '属性描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_addr` (`product_id`, `addr`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_attr_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品属性表';

-- ----------------------------
-- Table: 产品命令表
-- ----------------------------
DROP TABLE IF EXISTS `tb_command`;
CREATE TABLE `tb_command` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '命令ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `addr` varchar(50) NOT NULL COMMENT '命令属性标识符',
  `command_name` varchar(100) NOT NULL COMMENT '命令名称',
  `command_value` varchar(50) NOT NULL COMMENT '命令值',
  `description` varchar(200) DEFAULT NULL COMMENT '命令描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_cmd_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品命令表';

-- ----------------------------
-- Table: 设备表
-- ----------------------------
DROP TABLE IF EXISTS `tb_device`;
CREATE TABLE `tb_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_code` varchar(100) NOT NULL COMMENT '设备编码（唯一标识）',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `group_id` bigint DEFAULT NULL COMMENT '所属分组ID ⭐️ 新增',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '在线状态：0-离线，1-在线',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `location` varchar(200) DEFAULT NULL COMMENT '设备位置',
  `tags` varchar(500) DEFAULT NULL COMMENT '设备标签（JSON格式）',
  `offline_timeout` int NOT NULL DEFAULT 300 COMMENT '离线超时时间（秒）',
  `alarm_config` text DEFAULT NULL COMMENT '告警配置(JSON格式): {level, conditions, notifyUsers, stackMode}',
  `alarm_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '告警是否启用(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_alarm_enabled` (`alarm_enabled`),
  CONSTRAINT `fk_device_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- ----------------------------
-- Table: 设备数据表（时序数据）
-- ----------------------------
DROP TABLE IF EXISTS `tb_device_data`;
CREATE TABLE `tb_device_data` (
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
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ----------------------------
-- Table: 命令下发记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_command_log`;
CREATE TABLE `tb_command_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(100) NOT NULL COMMENT '设备编码',
  `command_id` bigint NOT NULL COMMENT '命令ID',
  `addr` varchar(50) NOT NULL COMMENT '命令属性',
  `addrv` varchar(50) NOT NULL COMMENT '命令值',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-已发送，1-已执行，2-失败',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `response_time` datetime DEFAULT NULL COMMENT '响应时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='命令下发记录表';

-- ----------------------------
-- Table: 消息通知表
-- ----------------------------
DROP TABLE IF EXISTS `tb_notification`;
CREATE TABLE `tb_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `alarm_id` bigint NOT NULL COMMENT '关联报警ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编码',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `alarm_level` varchar(20) NOT NULL COMMENT '报警级别：critical/warning/info',
  `alarm_message` text NOT NULL COMMENT '报警消息',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_alarm_id` (`alarm_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_user_read_time` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- ----------------------------
-- Table: 告警日志表
-- ----------------------------
DROP TABLE IF EXISTS `tb_alarm_log`;
CREATE TABLE `tb_alarm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编码',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `metric` varchar(50) DEFAULT NULL COMMENT '监控指标（属性标识符）',
  `trigger_operator` varchar(10) DEFAULT NULL COMMENT '触发时的运算符（用于恢复判断）',
  `trigger_threshold` double DEFAULT NULL COMMENT '触发时的阈值（用于恢复判断）',
  `notify_users` text DEFAULT NULL COMMENT '通知人员ID列表（JSON格式）',
  `alarm_level` varchar(20) NOT NULL COMMENT '告警级别：critical/warning/info',
  `alarm_message` text NOT NULL COMMENT '告警消息（描述触发的条件和实际值）',
  `trigger_time` datetime NOT NULL COMMENT '触发时间',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处理状态：0-未处理，1-已处理',
  `recovered` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已恢复：0-未恢复，1-已恢复',
  `handler` varchar(50) DEFAULT NULL COMMENT '处理人',
  `handle_description` text DEFAULT NULL COMMENT '处理描述',
  `handle_images` longtext DEFAULT NULL COMMENT '处理图片（JSON格式）',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_alarm_level` (`alarm_level`),
  KEY `idx_trigger_time` (`trigger_time`),
  KEY `idx_status` (`status`),
  KEY `idx_device_status_time` (`device_id`, `status`, `trigger_time`),
  KEY `idx_device_metric_recovered` (`device_id`, `metric`, `recovered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警日志表';

-- ----------------------------
-- 初始化示例数据
-- ----------------------------
-- 示例产品：温湿度传感器
INSERT INTO `tb_product` (`id`, `product_name`, `product_model`, `protocol`, `description`) 
VALUES (1, '温湿度传感器', 'TEM', 'MQTT', '用于监测环境温湿度');

-- 示例属性
INSERT INTO `tb_attribute` (`product_id`, `addr`, `attr_name`, `data_type`, `unit`) VALUES
(1, 'tem', '温度', 'float', '℃'),
(1, 'hum', '湿度', 'float', '%'),
(1, 'window', '窗户状态', 'int', NULL);

-- 示例命令
INSERT INTO `tb_command` (`product_id`, `addr`, `command_name`, `command_value`, `description`) VALUES
(1, 'window', '打开窗户', '1', '将窗户设置为打开状态'),
(1, 'window', '关闭窗户', '0', '将窗户设置为关闭状态');

-- 初始化超级管理员账户：admin / admin123456 （归属总分组）
INSERT INTO `tb_user` (`id`, `username`, `password`, `real_name`, `group_id`, `status`, `role_id`) VALUES
(1, 'admin', 'admin123456', '超级管理员', 1, 1, 1);

-- 初始化角色：仅保留超级管理员
INSERT INTO `tb_role` (`id`, `role_name`, `role_code`, `description`, `menu_ids`, `data_scope`) VALUES
(1, '超级管理员', 'super_admin', '拥有所有权限', 'dashboard,device,group,product,user,role,menu,log', 0);

-- 初始化菜单权限
INSERT INTO `tb_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`) VALUES
(1, 0, '数据监控', 2, '/dashboard', 'Dashboard', 'dashboard:view', 'DataLine', 1),
(2, 0, '设备管理', 1, '/device', NULL, NULL, 'Monitor', 2),
(3, 2, '设备列表', 2, '/device/list', 'DeviceList', 'device:list', '', 1),
(4, 2, '设备分组', 2, '/device/group', 'DeviceGroup', 'group:list', '', 2),
(5, 0, '产品管理', 2, '/product', 'ProductList', 'product:list', 'Box', 3),
(6, 0, '系统管理', 1, '/system', NULL, NULL, 'Setting', 4),
(7, 6, '用户管理', 2, '/system/user', 'UserList', 'user:list', '', 1),
(8, 6, '角色管理', 2, '/system/role', 'RoleList', 'role:list', '', 2),
(9, 6, '菜单管理', 2, '/system/menu', 'MenuList', 'menu:list', '', 3),
(10, 6, '操作日志', 2, '/system/log', 'LogList', 'log:list', '', 4);

-- 初始化设备分组（只保留总分组）
INSERT INTO `tb_device_group` (`id`, `parent_id`, `group_name`, `description`) VALUES
(1, 0, '总分组', '系统默认总分组，所有设备和用户的根分组');

SET FOREIGN_KEY_CHECKS = 1;
