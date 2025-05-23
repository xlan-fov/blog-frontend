package com.blog.config;

import com.blog.utils.JwtInterceptor;
import com.blog.utils.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: 注册拦截器
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                //.addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/users/login",
                        "/users/register",
                        "/users/captcha",
                        "/users/slider-image",
                        "/users/slider-validate",
                        "/users/logout").order(1); // 登录、注册不拦截

        registry.addInterceptor(new JwtInterceptor(stringRedisTemplate))
                .addPathPatterns("./**").order(0);
    }

    /**
     * 配置跨域请求
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
            .allowedOrigins("http://localhost:5174") // 前端开发服务器地址
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
            .allowedHeaders("*") // 允许所有请求头
            .allowCredentials(true) // 允许携带凭证(cookies)
            .maxAge(3600); // 预检请求的有效期(秒)
    }
}
