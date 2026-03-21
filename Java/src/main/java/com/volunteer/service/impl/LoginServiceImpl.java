package com.volunteer.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.LoginDTO;
import com.volunteer.dto.RegisterDTO;
import com.volunteer.entity.User;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.LoginService;
import com.volunteer.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录服务实现类
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.volunteer.mapper.VolunteerProfileMapper volunteerProfileMapper;

    @Autowired
    private com.volunteer.mapper.OrganizerProfileMapper organizerProfileMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis Key 前缀
    private static final String AUTH_TOKEN_PREFIX = "auth:token:";
    // Token 有效期（24小时）
    private static final long EXPIRE_TIME = 24;
    
    @Override
    public String login(LoginDTO loginDTO) {
        log.info("开始处理登录请求: username={}", loginDTO.getUsername());

        // 1. 根据用户名查询用户
        User user = null;
        try {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, loginDTO.getUsername()));
            log.info("数据库查询结果: {}", user != null ? "用户存在" : "用户不存在");
        } catch (Exception e) {
            log.error("数据库查询异常: {}", e.getMessage(), e);
            throw new ServiceException("系统错误：数据库查询失败");
        }
        
        if (user == null) {
            throw new ServiceException("用户名或密码错误");
        }
        
        // 2. 校验密码
        boolean passwordMatch = false;
        try {
            if (user.getPassword() != null && user.getPassword().equals(loginDTO.getPassword())) {
                passwordMatch = true;
            } else if (user.getPassword() != null && user.getPassword().equals(DigestUtil.md5Hex(loginDTO.getPassword()))) {
                passwordMatch = true;
            }
        } catch (Exception e) {
            log.error("密码校验异常: {}", e.getMessage(), e);
            // 这里不抛出系统错误，防止泄露过多信息，当作校验失败处理
        }

        if (!passwordMatch) {
            log.warn("密码不匹配: username={}", loginDTO.getUsername());
            throw new ServiceException("用户名或密码错误");
        }
        
        // 3. 校验状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("账号被禁用: userId={}", user.getUserId());
            throw new ServiceException("账号已被禁用");
        }
        
        // 4. 生成 Token
        String token = null;
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());
            token = jwtUtils.generateToken(claims);
            log.info("Token生成成功");
        } catch (Exception e) {
            log.error("Token生成异常: {}", e.getMessage(), e);
            throw new ServiceException("系统错误：Token生成失败");
        }
        
        // 5. 将用户信息存入 Redis
        try {
            user.setPassword(null); // 不存储密码
            redisTemplate.opsForValue().set(AUTH_TOKEN_PREFIX + token, user, EXPIRE_TIME, TimeUnit.HOURS);
            log.info("用户登录信息存入Redis成功");
        } catch (Exception e) {
            log.error("Redis写入异常 (请检查Redis服务状态): {}", e.getMessage(), e);
            throw new ServiceException("系统错误：登录状态保存失败");
        }
        
        return token;
    }

    @Override    public void resetPassword(com.volunteer.dto.ResetPasswordDTO resetPasswordDTO) {
        // 1. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, resetPasswordDTO.getUsername()));
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 2. 及其验证信息
        if ("volunteer".equals(user.getRole())) {
             com.volunteer.entity.VolunteerProfile profile = volunteerProfileMapper.selectById(user.getUserId());
             if (profile == null || profile.getPhone() == null || !profile.getPhone().equals(resetPasswordDTO.getSecurityAnswer())) {
                 throw new ServiceException("验证失败：预留手机号不匹配");
             }
        } else if ("organizer".equals(user.getRole())) {
             com.volunteer.entity.OrganizerProfile profile = organizerProfileMapper.selectById(user.getUserId());
             if (profile == null || profile.getOrgName() == null || !profile.getOrgName().equals(resetPasswordDTO.getSecurityAnswer())) {
                 throw new ServiceException("验证失败：组织名称不匹配");
             }
        } else {
             throw new ServiceException("该账号角色不支持自助重置密码，请联系管理员");
        }
        
        // 3. 更新密码
        user.setPassword(DigestUtil.md5Hex(resetPasswordDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override    public String register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否存在
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (existUser != null) {
            throw new ServiceException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 使用与登录一致的加密方式 (这里假设 MD5Hex)
        user.setPassword(DigestUtil.md5Hex(registerDTO.getPassword()));
        user.setRole(registerDTO.getRole()); // VOLUNTEER or ORGANIZER
        user.setStatus(1); // 默认启用
        
        // 只有志愿者才有积分和信用评分
        if ("VOLUNTEER".equalsIgnoreCase(registerDTO.getRole())) {
            user.setPoints(0);
            user.setCreditScore(100);
        } else {
            user.setPoints(null);
            user.setCreditScore(null);
        }
        
        user.setCreatedAt(LocalDateTime.now());
        
        // 3. 保存到数据库
        userMapper.insert(user);
        
        return null; // 不需要自动登录
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // 从 token 中去除 token 前缀 (通常是 "Bearer " + token)
            // 这里假设 logout 参数就是 raw token (不含 Bearer) 
            // 实际上 LoginController 传进来的就是 raw token ??
            // 还是需要去掉 Bearer ?
            // LoginController: String token = request.getHeader("Authorization");
            // request.getHeader("Authorization") returns "Bearer xxx" usually.
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            redisTemplate.delete(AUTH_TOKEN_PREFIX + token);
        }
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new ServiceException("密码不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        boolean match = false;
        // Check old password (plain text or MD5)
        if (oldPassword.equals(user.getPassword())) {
            match = true;
        } else if (cn.hutool.crypto.digest.DigestUtil.md5Hex(oldPassword).equals(user.getPassword())) {
            match = true;
        }

        if (!match) {
            throw new ServiceException("原密码错误");
        }

        user.setPassword(cn.hutool.crypto.digest.DigestUtil.md5Hex(newPassword));
        userMapper.updateById(user);
    }
}
