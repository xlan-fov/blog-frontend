package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ByteBlogAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ByteBlogAdminApplication.class, args);
    }
}
