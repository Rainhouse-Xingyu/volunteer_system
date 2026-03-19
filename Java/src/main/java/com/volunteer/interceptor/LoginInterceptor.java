package com.volunteer.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.common.Result;
import com.volunteer.entity.User;
import com.volunteer.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import com.volunteer.annotation.RequireRole;
import org.springframework.web.method.HandlerMethod;
import java.lang.reflect.Method;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String AUTH_TOKEN_PREFIX = "auth:token:";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法的操作，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 获取 Token
        String token = request.getHeader("Authorization");
        
        // 2. 校验 Token 是否存在
        if (!StringUtils.hasText(token)) {
            // 尝试从 Request Attribute 中获取 (有些 Filter 可能会处理)
            // 这里简单处理，如果头没有则返回未登录
            returnErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 3. 校验 Token 格式是否有效
        if (!jwtUtils.validateToken(token)) {
            returnErrorResponse(response, 401, "Token 无效，请重新登录");
            return false;
        }
        
        // 4. 校验 Redis 中是否存在（判断是否过期或被踢出）
        Object userObj = redisTemplate.opsForValue().get(AUTH_TOKEN_PREFIX + token);
        if (userObj == null) {
            returnErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        User user = (User) userObj;
        
        // 5. 刷新 Token 有效期 (续期)
        redisTemplate.expire(AUTH_TOKEN_PREFIX + token, 24, TimeUnit.HOURS);
        
        // 6. 将用户信息存入 Request 域
        request.setAttribute("currentUser", user);

        // 7. 权限校验
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RequireRole.class)) {
            RequireRole requireRole = method.getAnnotation(RequireRole.class);
            String role = requireRole.value();
            
            // 如果用户角色不匹配
            if (!role.equals(user.getRole())) {
                returnErrorResponse(response, 403, "权限不足，无法访问");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 返回错误响应
     */
    private void returnErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        
        Result<Void> result = Result.error(status, message);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        
        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.flush();
        writer.close();
    }
}
