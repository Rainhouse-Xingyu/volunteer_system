package com.volunteer.config;

import com.volunteer.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web 配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoginInterceptor loginInterceptor;
    
    // 白名单接口，不需要登录即可访问
    private static final List<String> WHITELIST = Arrays.asList(
            "/auth/login",
            "/auth/register",  // 如果有注册接口
            "/doc.html",       // 如果有 Swagger/Knife4j
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/error"
    );
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(WHITELIST); // 排除白名单
    }
}
