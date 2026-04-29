-- dbfound-world demo database initialization.
-- Run with:
--   mysql -uroot -p < examples/sql/init.sql

CREATE DATABASE IF NOT EXISTS dbfound
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE dbfound;

DROP TABLE IF EXISTS table2;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_code` varchar(100) NOT NULL,
  `user_name` varchar(200) NOT NULL,
  `password` varchar(50) NOT NULL DEFAULT '123456',
  `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_code` (`user_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `table2` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_table2_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL,
  `role_description` varchar(200) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `sys_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_code` varchar(100) NOT NULL,
  `user_name` varchar(200) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_sys_user_code` (`user_code`),
  KEY `idx_sys_user_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`user_code`, `user_name`, `password`, `create_by`, `create_date`) VALUES
  ('xiaoming', '小明', '123456', 1, NOW()),
  ('xiaohong', '小红', '123456', 1, NOW()),
  ('zhangsan', '张三', '123456', 1, NOW());

INSERT INTO `sys_role` (`role_code`, `role_description`) VALUES
  ('admin', '系统管理员'),
  ('user', '普通用户');

INSERT INTO `sys_user` (`user_code`, `user_name`, `role_id`) VALUES
  ('admin001', '管理员一号', 1),
  ('user001', '普通用户一号', 2);
