package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基础表
 */
@Data
@NoArgsConstructor
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 角色:志愿者/组织者/管理员
     */
    private String role;

    /**
     * 状态: 0禁用, 1正常
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
}
