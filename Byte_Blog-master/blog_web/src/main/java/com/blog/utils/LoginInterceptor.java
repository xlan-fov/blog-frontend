package com.blog.utils;

import com.blog.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("登录拦截器检查请求: {}", request.getRequestURI());
        
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            log.warn("用户未登录，拒绝访问: {}", request.getRequestURI());
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"用户未登录，请先登录\",\"data\":null}");
            return false;
        }
        
        log.debug("用户已登录: {}", user.getUsername());
        return true;
    }
}
