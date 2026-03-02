package com.volunteer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("志愿者管理系统接口文档")
                        .contact(new Contact().name("Volunteer System Team"))
                        .version("1.0")
                        .description("基于 Spring Boot 3 + Vue 3 的志愿者管理系统接口文档"));
    }
}
