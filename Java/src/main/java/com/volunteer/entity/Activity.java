package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动表
 */
@Data
@NoArgsConstructor
@TableName("activities")
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Integer activityId;

    /**
     * 组织者用户ID
     */
    private Integer organizerId;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动详情
     */
    private String content;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 地点
     */
    private String location;

    /**
     * 招募人数
     */
    private Integer quota;

    /**
     * 当前已报名人数
     */
    private Integer currentParticipants;

    /**
     * 0待审, 1招募中, 2进行中, 3已结束, 4审核失败
     */
    private Integer status;

    /**
     * 签到校验码
     */
    private String qrCodeToken;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
