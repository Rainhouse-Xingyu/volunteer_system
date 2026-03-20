package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)		
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)		
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 活动积分奖励
     */
    private Integer rewardPoints;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String organizerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String organizerAvatar;
}
