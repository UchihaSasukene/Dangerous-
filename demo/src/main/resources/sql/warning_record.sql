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

-- 插入测试数据
INSERT INTO `warning_record` (`chemical_id`, `warning_type`, `warning_level`, `warning_content`, `status`, `warning_time`, `handle_time`, `handler`, `handle_result`, `create_time`) VALUES
(1, 'low', 'urgent', '硫酸库存不足，当前库存低于预警阈值，请及时补充', 'unprocessed', '2025-03-10 10:00:00', NULL, NULL, NULL, '2025-03-10 10:00:00'),
(2, 'stock', 'serious', '盐酸库存接近预警阈值，请注意库存管理', 'processing', '2025-03-09 14:30:00', NULL, '张三', NULL, '2025-03-09 14:30:00'),
(3, 'high', 'normal', '乙醇库存超出安全存储量，请注意安全风险', 'processed', '2025-03-08 09:15:00', '2025-03-08 11:20:00', '李四', '已调整存储方案，分散存储降低风险', '2025-03-08 09:15:00'),
(1, 'low', 'serious', '硫酸库存持续下降，即将耗尽', 'unprocessed', '2025-03-10 16:45:00', NULL, NULL, NULL, '2025-03-10 16:45:00'),
(4, 'stock', 'normal', '氢氧化钠库存接近预警阈值', 'processed', '2025-03-07 13:20:00', '2025-03-07 15:30:00', '王五', '已下单补充库存，预计3天后到货', '2025-03-07 13:20:00'),
(5, 'high', 'urgent', '甲醇库存严重超标，存在重大安全隐患', 'processing', '2025-03-10 08:30:00', NULL, '赵六', NULL, '2025-03-10 08:30:00'),
(6, 'low', 'urgent', '丙酮库存不足，影响正常生产', 'unprocessed', '2025-03-10 11:10:00', NULL, NULL, NULL, '2025-03-10 11:10:00'),
(7, 'stock', 'serious', '二氯甲烷库存接近预警阈值', 'unprocessed', '2025-03-09 16:20:00', NULL, NULL, NULL, '2025-03-09 16:20:00'),
(8, 'high', 'normal', '乙醚库存超出安全存储量', 'processed', '2025-03-06 10:45:00', '2025-03-06 14:15:00', '钱七', '已转移部分库存至备用仓库', '2025-03-06 10:45:00'),
(9, 'low', 'urgent', '四氢呋喃库存不足，请紧急补充', 'processing', '2025-03-10 09:50:00', NULL, '孙八', NULL, '2025-03-10 09:50:00'),
(10, 'stock', 'normal', '乙酸库存接近预警阈值', 'processed', '2025-03-05 15:30:00', '2025-03-05 17:00:00', '周九', '已安排采购补充库存', '2025-03-05 15:30:00'); 