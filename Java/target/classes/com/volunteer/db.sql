/*
 Navicat Premium Dump SQL

 Source Server         : Alibaba_Ulanqab_Server
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42-0ubuntu0.20.04.1)
 Source Host           : mysql.rainhouse.me:3306
 Source Schema         : volunteersy

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42-0ubuntu0.20.04.1)
 File Encoding         : 65001

 Date: 11/03/2026 09:52:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities` (
  `activity_id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `organizer_id` int NOT NULL COMMENT '组织者用户ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `content` text NOT NULL COMMENT '活动详情',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(255) NOT NULL COMMENT '地点',
  `quota` int DEFAULT '0' COMMENT '招募人数',
  `reward_points` int DEFAULT '0' COMMENT '活动积分奖励',
  `status` tinyint DEFAULT '0' COMMENT '0待审, 1招募中, 2进行中, 3已结束, 4审核失败',
  `qr_code_token` varchar(64) DEFAULT NULL COMMENT '签到校验码',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `current_participants` int DEFAULT '0' COMMENT 'count of current participants',
  PRIMARY KEY (`activity_id`),
  KEY `fk_act_org` (`organizer_id`),
  CONSTRAINT `fk_act_org` FOREIGN KEY (`organizer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '评论人ID',
  `target_type` enum('activity','story') NOT NULL COMMENT '评论对象类型: 活动/故事',
  `target_id` int NOT NULL COMMENT '对应的活动ID或故事ID',
  `score` tinyint DEFAULT NULL COMMENT '评分(仅限活动评论时使用, 故事评论可为空)',
  `content` text NOT NULL COMMENT '评论内容',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-待审核, 1-正常显示, 2-违规隐藏',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用评论与反馈表';

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `notice_id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int DEFAULT '0' COMMENT '发送者(0系统)',
  `receiver_id` int DEFAULT '0' COMMENT '接收者(0全平台)',
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `type` enum('info','system_msg','notice') DEFAULT 'info' COMMENT '资讯/系统通知/公告',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) DEFAULT (0),
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for organizer_profiles
-- ----------------------------
DROP TABLE IF EXISTS `organizer_profiles`;
CREATE TABLE `organizer_profiles` (
  `user_id` int NOT NULL COMMENT '关联users表ID',
  `org_name` varchar(100) NOT NULL COMMENT '组织/社团名称',
  `description` text COMMENT '组织简介',
  `is_verified` tinyint DEFAULT '0' COMMENT '审核状态: 0未审, 1通过',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_org_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for registrations
-- ----------------------------
DROP TABLE IF EXISTS `registrations`;
CREATE TABLE `registrations` (
  `reg_id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL COMMENT '关联活动',
  `volunteer_id` int NOT NULL COMMENT '关联志愿者',
  `reg_status` tinyint DEFAULT '0' COMMENT '0:待审核, 1:已录用, 2:已拒绝',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `is_completed` tinyint DEFAULT '0' COMMENT '1表示确认完成',
  `checkin_status` tinyint DEFAULT '0' COMMENT '签到状态: 0-未签到, 1-已签到',
  `checkin_time` datetime DEFAULT NULL COMMENT '签到时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`reg_id`),
  KEY `fk_reg_act` (`activity_id`),
  KEY `fk_reg_vol` (`volunteer_id`),
  CONSTRAINT `fk_reg_act` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`activity_id`),
  CONSTRAINT `fk_reg_vol` FOREIGN KEY (`volunteer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像图片路径或URL',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `role` enum('volunteer','organizer','admin') NOT NULL COMMENT '角色:志愿者/组织者/管理员',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用, 1正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `points` int DEFAULT '0' COMMENT '志愿者积分',
  `cred` int DEFAULT '100' COMMENT '信用评分(满分100)',
  `level` varchar(20) DEFAULT '初级志愿者' COMMENT '荣誉等级称号',
  `bio` varchar(255) DEFAULT NULL COMMENT '个人简介/座右铭',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for violations
-- ----------------------------
DROP TABLE IF EXISTS `violations`;
CREATE TABLE `violations` (
  `violation_id` int NOT NULL AUTO_INCREMENT,
  `target_type` varchar(20) DEFAULT NULL COMMENT '违规对象类型(user/activity)',
  `target_id` int DEFAULT NULL COMMENT '对应ID',
  `reason` text COMMENT '处理原因',
  `handler_id` int DEFAULT NULL COMMENT '操作管理员ID',
  `processed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`violation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for volunteer_profiles
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_profiles`;
CREATE TABLE `volunteer_profiles` (
  `user_id` int NOT NULL COMMENT '关联users表ID',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `student_id` varchar(20) DEFAULT NULL COMMENT '学号',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `credit_score` int DEFAULT '100' COMMENT '信用评分',
  `points` int DEFAULT '0' COMMENT '志愿积分',
  `bio` text COMMENT '个人简介',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `student_id` (`student_id`),
  CONSTRAINT `fk_vol_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `news_id` int NOT NULL AUTO_INCREMENT,
  `organizer_id` int NOT NULL COMMENT '发布者/组织者ID',
  `title` varchar(255) NOT NULL COMMENT '资讯标题',
  `content` text NOT NULL COMMENT '资讯内容',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `views` int DEFAULT '0' COMMENT '阅读量',
  `status` tinyint DEFAULT '1' COMMENT '1发布, 0草稿',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`news_id`),
  KEY `fk_news_org` (`organizer_id`),
  CONSTRAINT `fk_news_org` FOREIGN KEY (`organizer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资讯/文章表';
