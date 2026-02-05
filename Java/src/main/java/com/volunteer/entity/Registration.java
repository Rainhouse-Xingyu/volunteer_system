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
 * 报名签到表
 */
@Data
@NoArgsConstructor
@TableName("registrations")
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "reg_id", type = IdType.AUTO)
    private Integer regId;

    /**
     * 关联活动
     */
    private Integer activityId;

    /**
     * 关联志愿者
     */
    private Integer volunteerId;

    /**
     * 0待录取, 1已录取, 2已拒绝
     */
    private Integer regStatus;

    /**
     * 签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkInTime;

    /**
     * 签到状态: 0未签到, 1已签到
     */
    private Integer checkinStatus;

    /**
     * 1表示确认完成
     */
    private Integer isCompleted;
}
