package com.blog.config;

import com.blog.utils.JwtInterceptor;
import com.blog.utils.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT拦截器先执行，用于解析token并设置用户信息
        registry.addInterceptor(new JwtInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/users/registerByname",
                        "/api/users/loginByname", 
                        "/api/users/loginByphone",
                        "/api/users/adminLogin",
                        "/api/users/captcha",
                        "/api/users/code",
                        "/api/users/slider-image",
                        "/api/users/slider-validate",
                        "/api/health",
                        "/error"
                ).order(0);

        // 登录拦截器后执行，检查是否需要登录
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns(
                        "/api/blogs/**"
                ).excludePathPatterns(
                        "/api/users/**",
                        "/api/health",
                        "/api/common/upload",
                        "/error"
                ).order(1);
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
