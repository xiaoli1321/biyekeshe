package com.learnplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 基于知识图谱的编程学习平台
 * Spring Boot 主应用入口
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LearnCourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnCourseApplication.class, args);
    }

}
