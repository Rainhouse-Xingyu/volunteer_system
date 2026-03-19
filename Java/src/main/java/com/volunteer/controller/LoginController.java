package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.dto.LoginDTO;
import com.volunteer.dto.RegisterDTO;
import com.volunteer.entity.User;
import com.volunteer.service.LoginService;
import com.volunteer.mapper.UserMapper; // Added
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
    
    @Autowired
    private UserMapper userMapper; // Added
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        String token = loginService.login(loginDTO);
        return Result.success("登录成功", token);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated RegisterDTO registerDTO) {
        // 如果注册后即登录，可以返回 token
        loginService.register(registerDTO);
        return Result.success("注册成功", null);
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

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        // 重新从数据库获取最新信息，避免 Redis 缓存未及时更新导致的信息陈旧
        User user = userMapper.selectById(currentUser.getUserId());
        if (user != null) {
            user.setPassword(null); // 脱敏
            return Result.success(user);
        }
        
        currentUser.setPassword(null); // 脱敏
        return Result.success(currentUser);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody @Validated com.volunteer.dto.PasswordDTO passwordDTO, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        loginService.changePassword(currentUser.getUserId(), passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success();
    }
}
