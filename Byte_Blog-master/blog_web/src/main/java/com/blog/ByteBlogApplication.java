package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.blog.mapper")
public class ByteBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(ByteBlogApplication.class, args);
    }
}
