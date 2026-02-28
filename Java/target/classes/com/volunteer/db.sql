-- 1. 用户基础表 (统一管理账号)
CREATE TABLE `users` (
  `user_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一标识',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
  `role` ENUM('volunteer', 'organizer', 'admin') NOT NULL COMMENT '角色:志愿者/组织者/管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用, 1正常',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 2. 志愿者详细资料表
CREATE TABLE `volunteer_profiles` (
  `user_id` INT PRIMARY KEY COMMENT '关联users表ID',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `student_id` VARCHAR(20) UNIQUE COMMENT '学号',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `credit_score` INT DEFAULT 100 COMMENT '信用评分',
  `points` INT DEFAULT 0 COMMENT '志愿积分',
  `bio` TEXT COMMENT '个人简介',
  CONSTRAINT `fk_vol_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 活动组织者资料表
CREATE TABLE `organizer_profiles` (
  `user_id` INT PRIMARY KEY COMMENT '关联users表ID',
  `org_name` VARCHAR(100) NOT NULL COMMENT '组织/社团名称',
  `description` TEXT COMMENT '组织简介',
  `is_verified` TINYINT DEFAULT 0 COMMENT '审核状态: 0未审, 1通过',
  CONSTRAINT `fk_org_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 活动表
CREATE TABLE `activities` (
  `activity_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
  `organizer_id` INT NOT NULL COMMENT '组织者用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
  `content` TEXT NOT NULL COMMENT '活动详情',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `location` VARCHAR(255) NOT NULL COMMENT '地点',
  `quota` INT DEFAULT 0 COMMENT '招募人数',
  `status` TINYINT DEFAULT 0 COMMENT '0待审, 1招募中, 2进行中, 3已结束, 4审核失败',
  `qr_code_token` VARCHAR(64) COMMENT '签到校验码',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_act_org` FOREIGN KEY (`organizer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 报名签到表
CREATE TABLE `registrations` (
  `reg_id` INT AUTO_INCREMENT PRIMARY KEY,
  `activity_id` INT NOT NULL COMMENT '关联活动',
  `volunteer_id` INT NOT NULL COMMENT '关联志愿者',
  `reg_status` TINYINT DEFAULT 0 COMMENT '0待录取, 1已录取, 2已拒绝',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `is_completed` TINYINT DEFAULT 0 COMMENT '1表示确认完成',
  CONSTRAINT `fk_reg_act` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`activity_id`),
  CONSTRAINT `fk_reg_vol` FOREIGN KEY (`volunteer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 评价表
CREATE TABLE `evaluations` (
  `eval_id` INT AUTO_INCREMENT PRIMARY KEY,
  `activity_id` INT NOT NULL,
  `from_user_id` INT NOT NULL COMMENT '评价人',
  `to_user_id` INT NOT NULL COMMENT '被评价人',
  `score` TINYINT DEFAULT 5 COMMENT '评分1-5',
  `comment` TEXT COMMENT '评价文字内容',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 资讯与系统通知表
CREATE TABLE `notifications` (
  `notice_id` INT AUTO_INCREMENT PRIMARY KEY,
  `sender_id` INT DEFAULT 0 COMMENT '发送者(0系统)',
  `receiver_id` INT DEFAULT 0 COMMENT '接收者(0全平台)',
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `type` ENUM('info', 'system_msg', 'notice') DEFAULT 'info' COMMENT '资讯/系统通知/公告',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 违规处理记录表
CREATE TABLE `violations` (
  `violation_id` INT AUTO_INCREMENT PRIMARY KEY,
  `target_type` VARCHAR(20) COMMENT '违规对象类型(user/activity)',
  `target_id` INT COMMENT '对应ID',
  `reason` TEXT COMMENT '处理原因',
  `handler_id` INT COMMENT '操作管理员ID',
  `processed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '评价人ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    score TINYINT NOT NULL DEFAULT 5 COMMENT '评分1-5星',
    content TEXT COMMENT '评价内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT '活动评价表';

CREATE TABLE `notifications` (
  `notice_id` INT AUTO_INCREMENT PRIMARY KEY,
  `sender_id` INT DEFAULT 0 COMMENT '发送者ID(0代表系统管理员)',
  `receiver_id` INT DEFAULT 0 COMMENT '接收者ID(0代表全平台公告)',
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `type` ENUM('info', 'system_msg', 'notice') DEFAULT 'info' COMMENT 'info:资讯, system_msg:系统通知, notice:公告',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读(0:未读, 1:已读)', -- 建议增加
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE registrations ADD COLUMN checkin_status TINYINT DEFAULT 0 COMMENT '签到状态: 0-未签到, 1-已签到';
ALTER TABLE registrations ADD COLUMN checkin_time DATETIME COMMENT '签到时间';

UPDATE registrations SET reg_status = 0 WHERE activity_id = 101 AND volunteer_id = 10;
ALTER TABLE registrations MODIFY COLUMN reg_status TINYINT DEFAULT 0 COMMENT '0:待审核, 1:已录用, 2:已拒绝';

ALTER TABLE users 
ADD COLUMN points INT DEFAULT 0 COMMENT '志愿者积分',
ADD COLUMN credit_score INT DEFAULT 100 COMMENT '信用评分(满分100)',
ADD COLUMN level VARCHAR(20) DEFAULT '初级志愿者' COMMENT '荣誉等级称号',
ADD COLUMN bio VARCHAR(255) COMMENT '个人简介/座右铭';

ALTER TABLE notifications
ADD COLUMN is_read TINYINT(1) DEFAULT(0) COMMIT "是否已读(0:未读, 1:已读)"