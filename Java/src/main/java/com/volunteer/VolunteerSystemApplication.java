package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 志愿者管理系统启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.volunteer.mapper")
public class VolunteerSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolunteerSystemApplication.class, args);
        System.out.println("\n======================================");
        System.out.println("志愿者管理系统启动成功！");
        System.out.println("访问地址：http://localhost:8080/");
        System.out.println("======================================\n");
    }

}
