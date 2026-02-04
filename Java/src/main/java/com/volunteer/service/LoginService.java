package com.volunteer.service;

import com.volunteer.dto.LoginDTO;

/**
 * 登录服务接口
 */
public interface LoginService {
    
    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return Token
     */
    String login(LoginDTO loginDTO);
    
    /**
     * 用户登出
     * @param token 用户 Token
     */
    void logout(String token);
}
