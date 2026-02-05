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
     * 联系电话
     */
    private String phone;

    /**
     * 信用评分 (数据已迁移至 users 表，此处仅作为 DTO 字段返回)
     */
    @TableField(exist = false)
    private Integer creditScore;

    /**
     * 志愿积分 (数据已迁移至 users 表，此处仅作为 DTO 字段返回)
     */
    @TableField(exist = false)
    private Integer points;

    /**
     * 个人简介
     */
    private String bio;
}
