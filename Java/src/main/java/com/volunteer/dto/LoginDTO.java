package com.volunteer.dto;

import lombok.Data;

/**
 * 登录请求参数封装
 */
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
