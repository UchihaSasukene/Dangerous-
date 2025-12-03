-- 创建化学品表（如果不存在）
CREATE TABLE IF NOT EXISTS `chemical` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '化学品ID',
  `name` varchar(100) NOT NULL COMMENT '化学品名称',
  `category` varchar(50) NOT NULL COMMENT '类别',
  `danger_level` varchar(50) NOT NULL COMMENT '危险等级',
  `storage_condition` varchar(100) NOT NULL COMMENT '存储条件',
  `warning_threshold` decimal(10,2) NOT NULL COMMENT '预警阈值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='化学品表';

-- 创建登录用户表（如果不存在）
CREATE TABLE IF NOT EXISTS `user_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(100) NOT NULL COMMENT '登录邮箱',
  `name` varchar(50) NOT NULL COMMENT '昵称/姓名',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `user_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '用户类型：0-普通，1-管理员',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统登录用户表';

-- 创建注册流水表（如果不存在）
CREATE TABLE IF NOT EXISTS `user_register_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `email` varchar(100) NOT NULL COMMENT '注册邮箱',
  `name` varchar(50) NOT NULL COMMENT '注册姓名',
  `user_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '注册类型',
  `register_ip` varchar(64) DEFAULT NULL COMMENT '注册IP',
  `register_channel` varchar(32) DEFAULT 'web' COMMENT '注册渠道',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户注册流水表';

-- 创建人员表（如果不存在）
CREATE TABLE IF NOT EXISTS `man` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '人员ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员表';

-- 创建库存表（如果不存在）
CREATE TABLE IF NOT EXISTS `inventory` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `current_amount` decimal(10,2) NOT NULL COMMENT '当前数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `location` varchar(100) DEFAULT NULL COMMENT '存储位置',
  `last_check_time` datetime DEFAULT NULL COMMENT '最后检查时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chemical_id` (`chemical_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 插入化学品测试数据（如果表为空）
INSERT INTO `chemical` (`name`, `category`, `danger_level`, `storage_condition`, `warning_threshold`, `description`)
SELECT * FROM (
  SELECT '硫酸' as name, '无机酸' as category, '高危' as danger_level, '阴凉干燥' as storage_condition, 5.00 as warning_threshold, '强酸，具有强烈的腐蚀性' as description UNION ALL
  SELECT '盐酸' as name, '无机酸' as category, '高危' as danger_level, '阴凉干燥' as storage_condition, 8.00 as warning_threshold, '强酸，具有腐蚀性' as description UNION ALL
  SELECT '乙醇' as name, '有机溶剂' as category, '中危' as danger_level, '阴凉避光' as storage_condition, 10.00 as warning_threshold, '易燃液体' as description UNION ALL
  SELECT '氢氧化钠' as name, '碱类' as category, '中危' as danger_level, '密封干燥' as storage_condition, 6.00 as warning_threshold, '强碱，具有腐蚀性' as description UNION ALL
  SELECT '甲醇' as name, '有机溶剂' as category, '高危' as danger_level, '阴凉避光' as storage_condition, 7.00 as warning_threshold, '易燃液体，有毒' as description UNION ALL
  SELECT '丙酮' as name, '有机溶剂' as category, '中危' as danger_level, '阴凉避光' as storage_condition, 12.00 as warning_threshold, '易燃液体' as description UNION ALL
  SELECT '二氯甲烷' as name, '有机溶剂' as category, '中危' as danger_level, '阴凉避光' as storage_condition, 8.00 as warning_threshold, '有毒液体' as description UNION ALL
  SELECT '乙醚' as name, '有机溶剂' as category, '高危' as danger_level, '阴凉避光' as storage_condition, 5.00 as warning_threshold, '极易燃液体' as description UNION ALL
  SELECT '四氢呋喃' as name, '有机溶剂' as category, '中危' as danger_level, '阴凉避光' as storage_condition, 6.00 as warning_threshold, '易燃液体' as description UNION ALL
  SELECT '乙酸' as name, '有机酸' as category, '中危' as danger_level, '阴凉干燥' as storage_condition, 9.00 as warning_threshold, '有腐蚀性' as description
) as tmp
WHERE NOT EXISTS (SELECT 1 FROM `chemical` LIMIT 1);

-- 插入登录用户测试数据（如果表为空）
INSERT INTO `user_account` (`email`, `name`, `password`, `user_type`, `status`)
SELECT * FROM (
  SELECT 'admin@example.com' AS email, '系统管理员' AS name, '$2a$10$7omPvKgJo0vvV.W5AO9Mcu.buk..qS0bkkCm1cW0XkyR3O6jwxKF2' AS password, 1 AS user_type, 1 AS status UNION ALL
  SELECT 'user@example.com' AS email, '普通用户' AS name, '$2a$10$7omPvKgJo0vvV.W5AO9Mcu.buk..qS0bkkCm1cW0XkyR3O6jwxKF2' AS password, 0 AS user_type, 1 AS status
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user_account` LIMIT 1);

-- 插入注册流水测试数据（如果表为空）
INSERT INTO `user_register_record` (`email`, `name`, `user_type`, `register_ip`, `register_channel`)
SELECT * FROM (
  SELECT 'admin@example.com', '系统管理员', 1, '127.0.0.1', 'init' UNION ALL
  SELECT 'user@example.com', '普通用户', 0, '127.0.0.1', 'init'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `user_register_record` LIMIT 1);

-- 插入人员测试数据（如果表为空）
INSERT INTO `man` (`name`, `gender`, `phone`, `email`, `department`, `position`, `create_time`)
SELECT * FROM (
  SELECT '张三' as name, '男' as gender, '13800138001' as phone, 'zhangsan@example.com' as email, '研发部' as department, '研究员' as position, NOW() as create_time UNION ALL
  SELECT '李四' as name, '男' as gender, '13800138002' as phone, 'lisi@example.com' as email, '研发部' as department, '助理研究员' as position, NOW() as create_time UNION ALL
  SELECT '王五' as name, '女' as gender, '13800138003' as phone, 'wangwu@example.com' as email, '质检部' as department, '质检员' as position, NOW() as create_time UNION ALL
  SELECT '赵六' as name, '男' as gender, '13800138004' as phone, 'zhaoliu@example.com' as email, '生产部' as department, '技术员' as position, NOW() as create_time UNION ALL
  SELECT '钱七' as name, '女' as gender, '13800138005' as phone, 'qianqi@example.com' as email, '安全部' as department, '安全员' as position, NOW() as create_time
) as tmp
WHERE NOT EXISTS (SELECT 1 FROM `man` LIMIT 1);

-- 插入库存测试数据（如果表为空）
INSERT INTO `inventory` (`chemical_id`, `current_amount`, `unit`, `location`, `last_check_time`, `create_time`)
SELECT * FROM (
  SELECT 1 as chemical_id, 4.50 as current_amount, 'L' as unit, 'A区-01架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 2 as chemical_id, 9.20 as current_amount, 'L' as unit, 'A区-02架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 3 as chemical_id, 15.30 as current_amount, 'L' as unit, 'B区-01架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 4 as chemical_id, 7.80 as current_amount, 'kg' as unit, 'B区-02架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 5 as chemical_id, 6.50 as current_amount, 'L' as unit, 'C区-01架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 6 as chemical_id, 3.20 as current_amount, 'L' as unit, 'C区-02架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 7 as chemical_id, 8.90 as current_amount, 'L' as unit, 'D区-01架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 8 as chemical_id, 4.10 as current_amount, 'L' as unit, 'D区-02架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 9 as chemical_id, 2.80 as current_amount, 'L' as unit, 'E区-01架' as location, NOW() as last_check_time, NOW() as create_time UNION ALL
  SELECT 10 as chemical_id, 10.50 as current_amount, 'L' as unit, 'E区-02架' as location, NOW() as last_check_time, NOW() as create_time
) as tmp
WHERE NOT EXISTS (SELECT 1 FROM `inventory` LIMIT 1);

-- 创建预警记录表
CREATE TABLE IF NOT EXISTS `warning_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `warning_type` varchar(50) NOT NULL COMMENT '预警类型：stock(库存预警)、low(库存不足)、high(超储预警)',
  `warning_level` varchar(50) NOT NULL COMMENT '预警等级：normal(一般)、serious(严重)、urgent(紧急)',
  `warning_content` varchar(500) NOT NULL COMMENT '预警内容',
  `status` varchar(50) NOT NULL DEFAULT 'unprocessed' COMMENT '处理状态：unprocessed(未处理)、processing(处理中)、processed(已处理)',
  `warning_time` datetime NOT NULL COMMENT '预警时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler` varchar(100) DEFAULT NULL COMMENT '处理人',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chemical_id` (`chemical_id`),
  KEY `idx_warning_type` (`warning_type`),
  KEY `idx_status` (`status`),
  KEY `idx_warning_time` (`warning_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- 创建使用记录表
CREATE TABLE IF NOT EXISTS `usage_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '使用记录ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `user_id` int(11) NOT NULL COMMENT '使用人ID',
  `amount` decimal(10,2) NOT NULL COMMENT '使用数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `usage_time` datetime NOT NULL COMMENT '使用时间',
  `usage_purpose` varchar(200) NOT NULL COMMENT '使用目的',
  `notes` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chemical_id` (`chemical_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_usage_time` (`usage_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用记录表';

-- 插入预警记录测试数据（如果表为空）
INSERT INTO `warning_record` (`chemical_id`, `warning_type`, `warning_level`, `warning_content`, `status`, `warning_time`, `handle_time`, `handler`, `handle_result`, `create_time`)
SELECT * FROM (
  SELECT 1 as chemical_id, 'low' as warning_type, 'urgent' as warning_level, '硫酸库存不足，当前库存低于预警阈值，请及时补充' as warning_content, 'unprocessed' as status, '2025-03-10 10:00:00' as warning_time, NULL as handle_time, NULL as handler, NULL as handle_result, '2025-03-10 10:00:00' as create_time UNION ALL
  SELECT 2 as chemical_id, 'stock' as warning_type, 'serious' as warning_level, '盐酸库存接近预警阈值，请注意库存管理' as warning_content, 'processing' as status, '2025-03-09 14:30:00' as warning_time, NULL as handle_time, '张三' as handler, NULL as handle_result, '2025-03-09 14:30:00' as create_time UNION ALL
  SELECT 3 as chemical_id, 'high' as warning_type, 'normal' as warning_level, '乙醇库存超出安全存储量，请注意安全风险' as warning_content, 'processed' as status, '2025-03-08 09:15:00' as warning_time, '2025-03-08 11:20:00' as handle_time, '李四' as handler, '已调整存储方案，分散存储降低风险' as handle_result, '2025-03-08 09:15:00' as create_time UNION ALL
  SELECT 1 as chemical_id, 'low' as warning_type, 'serious' as warning_level, '硫酸库存持续下降，即将耗尽' as warning_content, 'unprocessed' as status, '2025-03-10 16:45:00' as warning_time, NULL as handle_time, NULL as handler, NULL as handle_result, '2025-03-10 16:45:00' as create_time UNION ALL
  SELECT 4 as chemical_id, 'stock' as warning_type, 'normal' as warning_level, '氢氧化钠库存接近预警阈值' as warning_content, 'processed' as status, '2025-03-07 13:20:00' as warning_time, '2025-03-07 15:30:00' as handle_time, '王五' as handler, '已下单补充库存，预计3天后到货' as handle_result, '2025-03-07 13:20:00' as create_time
) as tmp
WHERE NOT EXISTS (SELECT 1 FROM `warning_record` LIMIT 1);

-- 插入使用记录测试数据（如果表为空）
INSERT INTO `usage_record` (`chemical_id`, `user_id`, `amount`, `unit`, `usage_time`, `usage_purpose`, `notes`, `create_time`)
SELECT * FROM (
  SELECT 1 as chemical_id, 1 as user_id, 2.50 as amount, 'L' as unit, '2025-03-10 09:30:00' as usage_time, '实验室测试' as usage_purpose, '用于酸碱中和实验' as notes, '2025-03-10 09:30:00' as create_time UNION ALL
  SELECT 2 as chemical_id, 2 as user_id, 1.00 as amount, 'L' as unit, '2025-03-10 10:15:00' as usage_time, '清洗设备' as usage_purpose, '用于清洗实验器材' as notes, '2025-03-10 10:15:00' as create_time UNION ALL
  SELECT 3 as chemical_id, 3 as user_id, 0.50 as amount, 'L' as unit, '2025-03-09 14:20:00' as usage_time, '样品制备' as usage_purpose, '用于样品溶解' as notes, '2025-03-09 14:20:00' as create_time UNION ALL
  SELECT 4 as chemical_id, 1 as user_id, 1.50 as amount, 'kg' as unit, '2025-03-09 11:30:00' as usage_time, '溶液配制' as usage_purpose, '配制标准溶液' as notes, '2025-03-09 11:30:00' as create_time UNION ALL
  SELECT 5 as chemical_id, 4 as user_id, 0.75 as amount, 'L' as unit, '2025-03-08 16:45:00' as usage_time, '实验分析' as usage_purpose, '用于色谱分析' as notes, '2025-03-08 16:45:00' as create_time
) as tmp
WHERE NOT EXISTS (SELECT 1 FROM `usage_record` LIMIT 1); 