package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 对应数据库表 comments
 */
@Data
@NoArgsConstructor
@TableName("comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 关联用户ID
     */
    private Integer userId;

    /**
     * 评论对象类型: activity/story
     */
    private String targetType;

    /**
     * 对应的活动ID或故事ID
     */
    private Integer targetId;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 状态: 0-待审核, 1-正常显示, 2-违规隐藏
     */
    private Integer status;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     * 使用 SQL 默认值：createdAt 为 null 时数据库自动填充 TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
