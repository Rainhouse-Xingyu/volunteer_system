package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 活动组织者资料表
 */
@Data
@NoArgsConstructor
@TableName("organizer_profiles")
public class OrganizerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联users表ID
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Integer userId;

    /**
     * 组织/社团名称
     */
    private String orgName;

    /**
     * 组织简介
     */
    private String description;

    /**
     * 审核状态: 0未审, 1通过
     */
    private Integer isVerified;

    /**
     * 用户昵称
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;

    /**
     * 用户头像
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String avatarUrl;
}
