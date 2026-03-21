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
     * 用户注册
     * @param registerDTO 注册参数
     * @return 注册成功后的 Token (自动登陆) 或 null
     */
    String register(com.volunteer.dto.RegisterDTO registerDTO);
    
    /**
     * 用户登出
     * @param token 用户 Token
     */
    void logout(String token);

    /**
     * 重置密码
     * @param resetPasswordDTO 重置密码参数
     */
    void resetPassword(com.volunteer.dto.ResetPasswordDTO resetPasswordDTO);

    /**
     * 修改密码
     * @param userId 用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);

}

