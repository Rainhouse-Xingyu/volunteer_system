package com.volunteer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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

    @Autowired
    private ObjectMapper objectMapper;
    
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

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        // 映射本地文件目录到 HTTP 路径
        // 这里的路径 file:./uploads/ 对应 FileUploadController 中的默认路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

    /**
     * 扩展消息转换器，确保使用我们自定义的 objectMapper
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 查找并替换默认的 Jackson 转换器，或者直接添加在最前面
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                converters.set(i, new MappingJackson2HttpMessageConverter(objectMapper));
                return;
            }
        }
        // 如果没找到（不太可能），则添加到最前面
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
