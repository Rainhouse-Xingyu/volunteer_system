package com.volunteer.dto;

import com.volunteer.entity.Activity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报名记录 DTO
 */
@Data
public class RegistrationDTO implements Serializable {
    
    private Integer regId;
    private Integer activityId;
    private Integer volunteerId;
    private Integer regStatus; // 0待录取, 1已录取, 2已拒绝, 3已取消
    private LocalDateTime checkInTime;
    private Integer checkinStatus; // 0未签到, 1已签到
    private String assessment; // 评价

    private LocalDateTime createTime; // Create Time

    // 活动信息快照
    private String activityTitle;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer rewardPoints; // 积分奖励

    // 招募人数
    private Integer quota;
    // 当前已报名人数
    private Integer currentParticipants;


    // 志愿者信息 (关联查询补充)
    private String volunteerName; // 志愿者姓名
    
    // Alias for frontend compatibility
    public String getUsername() {
        return this.volunteerName;
    }

    private String studentId;     // 学号
    private String phone;         // 手机号
    private String avatarUrl;     // 志愿者头像

    /**
     * 是否已评价
     */
    private Boolean hasCommented;
    
}
