package com.volunteer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论展示对象
 */
@Data
public class CommentVO {
    
    private Integer commentId;
    
    private Integer userId;
    
    private Integer activityId;
    
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    
    /**
     * 用户名 (关联查询)
     */
    private String username;

    /**
     * 用户昵称 (关联查询)
     */
    private String nickname;
    
    /**
     * 头像 (关联查询，目前数据库暂无，预留)
     */
    private String avatar;
    
    /**
     * 活动标题 (仅在我的评论列表需要)
     */
    private String activityTitle;
}
