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
 * 评论举报实体
 */
@Data
@NoArgsConstructor
@TableName("comment_reports")
public class CommentReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId;

    /**
     * 被举报的评论ID
     */
    private Integer commentId;

    /**
     * 举报人ID
     */
    private Integer reporterId;

    /**
     * 举报原因 (spam, abuse, etc.)
     */
    private String reason;

    /**
     * 详细说明
     */
    private String detail;

    /**
     * 处理状态: 0-待处理, 1-已处理(确认违规), 2-已驳回
     */
    private Integer status;
    
    /**
     * 处理备注
     */
    private String handleRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}