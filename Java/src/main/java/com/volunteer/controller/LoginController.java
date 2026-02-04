package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.LoginDTO;
import com.volunteer.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/auth")
public class LoginController {
    
    @Autowired
    private LoginService loginService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        String token = loginService.login(loginDTO);
        return Result.success("登录成功", token);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        loginService.logout(token);
        return Result.success("退出成功", null);
    }
}
