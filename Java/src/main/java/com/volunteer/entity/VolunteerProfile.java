package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 志愿者详细资料表
 */
@Data
@NoArgsConstructor
@TableName("volunteer_profiles")
public class VolunteerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联users表ID
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Integer userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 志愿积分
     */
    @TableField(exist = false)
    private Integer volunteerPoints; // 对应 users 表中的 points

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 信用评分
     */
    @TableField(exist = false)
    private Integer creditScore; // 对应 users 表中的 credit_score

    /**
     * 用户昵称
     */
    @TableField(exist = false)
    private String nickname;

    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;
    private Integer points;


}
