/*
SQLyog Ultimate v8.32 
MySQL - 8.0.16 : Database - vueone
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`vueone` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `vueone`;

/*Table structure for table `chemical` */

DROP TABLE IF EXISTS `chemical`;

CREATE TABLE `chemical` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '化学品ID',
  `name` varchar(100) NOT NULL COMMENT '化学品名称',
  `category` varchar(50) NOT NULL COMMENT '类别',
  `danger_level` varchar(50) NOT NULL COMMENT '危险等级',
  `storage_condition` varchar(100) NOT NULL COMMENT '存储条件',
  `warning_threshold` decimal(10,2) NOT NULL COMMENT '预警阈值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='化学品表';

/*Data for the table `chemical` */

insert  into `chemical`(`id`,`name`,`category`,`danger_level`,`storage_condition`,`warning_threshold`,`description`,`create_time`,`update_time`) values (1,'硫酸','无机酸','高危','阴凉干燥','5.00','强酸，具有强烈的腐蚀性','2025-12-03 18:20:39','2025-12-03 18:20:39'),(2,'盐酸','无机酸','高危','阴凉干燥','8.00','强酸，具有腐蚀性','2025-12-03 18:20:39','2025-12-03 18:20:39'),(3,'乙醇','有机溶剂','中危','阴凉避光','10.00','易燃液体','2025-12-03 18:20:39','2025-12-03 18:20:39'),(4,'氢氧化钠','碱类','中危','密封干燥','6.00','强碱，具有腐蚀性','2025-12-03 18:20:39','2025-12-03 18:20:39'),(5,'甲醇','有机溶剂','高危','阴凉避光','7.00','易燃液体，有毒','2025-12-03 18:20:39','2025-12-03 18:20:39');

/*Table structure for table `inventory` */

DROP TABLE IF EXISTS `inventory`;

CREATE TABLE `inventory` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `current_amount` decimal(10,2) NOT NULL COMMENT '当前数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `location` varchar(100) DEFAULT NULL COMMENT '存储位置',
  `last_check_time` datetime DEFAULT NULL COMMENT '最后检查时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chemical_id` (`chemical_id`),
  CONSTRAINT `inventory_ibfk_chem` FOREIGN KEY (`chemical_id`) REFERENCES `chemical` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';

/*Data for the table `inventory` */

insert  into `inventory`(`id`,`chemical_id`,`current_amount`,`unit`,`location`,`last_check_time`,`create_time`,`update_time`) values (1,1,'4.50','L','A区-01架','2025-12-03 18:20:39','2025-12-03 18:20:39','2025-12-03 18:20:39'),(2,2,'9.20','L','A区-02架','2025-12-03 18:20:39','2025-12-03 18:20:39','2025-12-03 18:20:39'),(3,3,'15.30','L','B区-01架','2025-12-03 18:20:39','2025-12-03 18:20:39','2025-12-03 18:20:39'),(4,4,'7.80','kg','B区-02架','2025-12-03 18:20:39','2025-12-03 18:20:39','2025-12-03 18:20:39'),(5,5,'6.50','L','C区-01架','2025-12-03 18:20:39','2025-12-03 18:20:39','2025-12-03 18:20:39');

/*Table structure for table `man` */

DROP TABLE IF EXISTS `man`;

CREATE TABLE `man` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '人员ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `password` varchar(100) DEFAULT NULL COMMENT '历史加密密码',
  `user_type` tinyint(4) DEFAULT '0' COMMENT '0-普通用户，1-管理员',
  `status` tinyint(4) DEFAULT '1' COMMENT '0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人员表';

/*Data for the table `man` */

insert  into `man`(`id`,`name`,`gender`,`phone`,`email`,`department`,`position`,`password`,`user_type`,`status`,`last_login_time`,`create_time`) values (1,'张三','男','14778305832','zhangsan@example.com','研发部','研究员',NULL,1,1,NULL,'2025-12-03 18:20:39'),(2,'李四','女','15778603582','lisi@example.com','研发部','助理研究员',NULL,0,1,NULL,'2025-12-03 18:20:39'),(3,'王五','男','14222235156','wangwu@example.com','质检部','质检员',NULL,0,1,NULL,'2025-12-03 18:20:39'),(4,'赵六','男','15123123123','zhaoliu@example.com','生产部','技术员',NULL,0,1,NULL,'2025-12-03 18:20:39'),(5,'钱七','女','13756158951','qianqi@example.com','安全部','安全员',NULL,0,1,NULL,'2025-12-03 18:20:39');

/*Table structure for table `outbound_record` */

DROP TABLE IF EXISTS `outbound_record`;

CREATE TABLE `outbound_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '出库记录ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `amount` decimal(10,2) NOT NULL COMMENT '出库数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号',
  `outbound_time` datetime NOT NULL COMMENT '出库时间',
  `operator_id` int(11) NOT NULL COMMENT '操作员ID',
  `recipient` varchar(100) NOT NULL COMMENT '领用人/部门',
  `purpose` varchar(200) DEFAULT NULL COMMENT '用途',
  `notes` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_outbound_chem` (`chemical_id`),
  KEY `idx_outbound_operator` (`operator_id`),
  CONSTRAINT `outbound_record_ibfk_chem` FOREIGN KEY (`chemical_id`) REFERENCES `chemical` (`id`),
  CONSTRAINT `outbound_record_ibfk_man` FOREIGN KEY (`operator_id`) REFERENCES `man` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出库记录表';

/*Data for the table `outbound_record` */

insert  into `outbound_record`(`id`,`chemical_id`,`amount`,`unit`,`batch_no`,`outbound_time`,`operator_id`,`recipient`,`purpose`,`notes`,`create_time`) values (1,1,'10.00','L','SUL2024031501','2025-03-15 10:00:00',1,'实验室A组','酸碱中和实验',NULL,'2025-12-03 18:20:39'),(2,2,'5.00','L','HCL2024031501','2025-03-15 14:30:00',2,'实验室B组','设备清洗',NULL,'2025-12-03 18:20:39'),(3,3,'8.00','L','ETH2024031501','2025-03-15 10:30:00',3,'实验室C组','样品制备',NULL,'2025-12-03 18:20:39');

/*Table structure for table `storage_record` */

DROP TABLE IF EXISTS `storage_record`;

CREATE TABLE `storage_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '入库记录ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `amount` decimal(10,2) NOT NULL COMMENT '入库数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号',
  `storage_time` datetime NOT NULL COMMENT '入库时间',
  `operator_id` int(11) NOT NULL COMMENT '操作员ID',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供货商',
  `notes` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_storage_chem` (`chemical_id`),
  KEY `idx_storage_operator` (`operator_id`),
  CONSTRAINT `storage_record_ibfk_chem` FOREIGN KEY (`chemical_id`) REFERENCES `chemical` (`id`),
  CONSTRAINT `storage_record_ibfk_man` FOREIGN KEY (`operator_id`) REFERENCES `man` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库记录表';

/*Data for the table `storage_record` */

insert  into `storage_record`(`id`,`chemical_id`,`amount`,`unit`,`batch_no`,`storage_time`,`operator_id`,`supplier`,`notes`,`create_time`) values (1,1,'50.00','L','SUL2024031501','2025-03-15 09:30:00',1,'华东化工有限公司','新批次硫酸入库','2025-12-03 18:20:39'),(2,3,'40.00','L','ETH2024031501','2025-03-15 10:15:00',3,'北方化工有限公司','新批次乙醇','2025-12-03 18:20:39'),(3,4,'35.00','kg','NAOH2024031501','2025-03-15 11:00:00',5,'西部化工有限公司','补充库存','2025-12-03 18:20:39');

/*Table structure for table `usage_record` */

DROP TABLE IF EXISTS `usage_record`;

CREATE TABLE `usage_record` (
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
  KEY `idx_usage_chem` (`chemical_id`),
  KEY `idx_usage_user` (`user_id`),
  CONSTRAINT `usage_record_ibfk_chem` FOREIGN KEY (`chemical_id`) REFERENCES `chemical` (`id`),
  CONSTRAINT `usage_record_ibfk_man` FOREIGN KEY (`user_id`) REFERENCES `man` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='使用记录表';

/*Data for the table `usage_record` */

insert  into `usage_record`(`id`,`chemical_id`,`user_id`,`amount`,`unit`,`usage_time`,`usage_purpose`,`notes`,`create_time`) values (1,1,1,'2.50','L','2025-03-10 09:30:00','实验室测试','用于酸碱中和实验','2025-12-03 18:20:40'),(2,2,2,'1.00','L','2025-03-10 10:15:00','清洗设备','用于清洗实验器材','2025-12-03 18:20:40'),(3,3,3,'0.50','L','2025-03-09 14:20:00','样品制备','用于样品溶解','2025-12-03 18:20:40'),(4,4,1,'1.50','kg','2025-03-09 11:30:00','溶液配制','配制标准溶液','2025-12-03 18:20:40');

/*Table structure for table `user_account` */

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(100) NOT NULL COMMENT '登录邮箱',
  `name` varchar(50) NOT NULL COMMENT '昵称/姓名',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `user_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-普通用户，1-管理员',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统登录用户表';

/*Data for the table `user_account` */

insert  into `user_account`(`id`,`email`,`name`,`password`,`user_type`,`status`,`last_login_time`,`create_time`,`update_time`) values (1,'admin','系统管理员','123456',1,1,'2025-12-03 18:37:16','2025-12-03 18:20:39','2025-12-03 18:37:16'),(2,'user@example.com','普通用户','$2a$10$7omPvKgJo0vvV.W5AO9Mcu.buk..qS0bkkCm1cW0XkyR3O6jwxKF2',0,1,NULL,'2025-12-03 18:20:39','2025-12-03 18:20:39');

/*Table structure for table `user_register_record` */

DROP TABLE IF EXISTS `user_register_record`;

CREATE TABLE `user_register_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `email` varchar(100) NOT NULL COMMENT '注册邮箱',
  `name` varchar(50) NOT NULL COMMENT '注册姓名',
  `user_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '注册类型',
  `register_ip` varchar(64) DEFAULT NULL COMMENT '注册IP',
  `register_channel` varchar(32) DEFAULT 'web' COMMENT '注册渠道',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户注册流水表';

/*Data for the table `user_register_record` */

insert  into `user_register_record`(`id`,`email`,`name`,`user_type`,`register_ip`,`register_channel`,`create_time`) values (1,'admin@example.com','系统管理员',1,'127.0.0.1','init','2025-12-03 18:20:39'),(2,'user@example.com','普通用户',0,'127.0.0.1','init','2025-12-03 18:20:39');

/*Table structure for table `warning_record` */

DROP TABLE IF EXISTS `warning_record`;

CREATE TABLE `warning_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
  `chemical_id` int(11) NOT NULL COMMENT '化学品ID',
  `warning_type` varchar(50) NOT NULL COMMENT '预警类型：stock、low、high',
  `warning_level` varchar(50) NOT NULL COMMENT '预警等级：normal、serious、urgent',
  `warning_content` varchar(500) NOT NULL COMMENT '预警内容',
  `status` varchar(50) NOT NULL DEFAULT 'unprocessed' COMMENT '处理状态',
  `warning_time` datetime NOT NULL COMMENT '预警时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler` varchar(100) DEFAULT NULL COMMENT '处理人',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_warning_chem` (`chemical_id`),
  KEY `idx_warning_type` (`warning_type`),
  KEY `idx_warning_status` (`status`),
  KEY `idx_warning_time` (`warning_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录表';

/*Data for the table `warning_record` */

insert  into `warning_record`(`id`,`chemical_id`,`warning_type`,`warning_level`,`warning_content`,`status`,`warning_time`,`handle_time`,`handler`,`handle_result`,`create_time`) values (1,1,'low','urgent','硫酸库存不足，当前库存低于预警阈值','unprocessed','2025-03-10 10:00:00',NULL,NULL,NULL,'2025-12-03 18:20:40'),(2,2,'stock','serious','盐酸库存接近预警阈值，请注意库存管理','processing','2025-03-09 14:30:00',NULL,'张三',NULL,'2025-12-03 18:20:40'),(3,3,'high','normal','乙醇库存超出安全存储量，请注意安全风险','processed','2025-03-08 09:15:00',NULL,'李四','已调整存储方案，分散存储降低风险','2025-12-03 18:20:40');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
