package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@NoArgsConstructor
@TableName("evaluations")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(value = "eval_id", type = IdType.AUTO)
    private Integer evalId;

    /**
     * 活动ID
     */
    private Integer activityId;

    /**
     * 评价人
     */
    private Integer fromUserId;

    /**
     * 被评价人
     */
    private Integer toUserId;

    /**
     * 评分1-5
     */
    private Integer score;

    /**
     * 评价文字内容
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
