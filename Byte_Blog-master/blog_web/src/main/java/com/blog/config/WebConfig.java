package com.blog.config;

import com.blog.utils.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LoginInterceptor loginInterceptor() {
        log.info("创建LoginInterceptor Bean");
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册拦截器");
        
        // 只注册登录拦截器，JWT验证由JwtAuthenticationFilter处理
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns(
                        "/api/blogs/addBlog",     // 创建博客
                        "/api/blogs/**",          // 所有博客相关操作
                        "/api/saveBlogContent",   // 保存博客内容
                        "/api/getDraft",          // 获取草稿
                        "/api/deleteBlogs/**",    // 删除博客
                        "/api/export/**",         // 导出功能
                        "/api/admin/**"           // 管理员相关
                )
                .excludePathPatterns(
                        "/api/users/**",          // 用户相关API不需要登录
                        "/api/health",            // 健康检查
                        "/api/common/upload",     // 上传
                        "/error"                  // 错误页面
                );
        
        log.info("登录拦截器注册完成");
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
