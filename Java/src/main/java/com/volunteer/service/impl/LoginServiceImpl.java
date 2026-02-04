package com.volunteer.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.LoginDTO;
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

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            redisTemplate.delete(AUTH_TOKEN_PREFIX + token);
        }
    }
}
