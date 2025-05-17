package com.blog.utils;


import com.auth0.jwt.interfaces.Claim;
import com.blog.dto.UserDTO;
import com.blog.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: Jwt拦截器
 */

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
//        System.out.println("token: " + token);

        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("未登录，token为空");
            return false;
        }

        Map<String, Claim> claims = JwtUtil.verifyToken(token);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("token无效或已过期");
            return false;
        }

        //解析用户信息存入threadlocal
        UserDTO user = new UserDTO();
        user.setId(claims.get("id").asInt());
        user.setUsername(claims.get("userName").asString());

        // 保存用户信息到 ThreadLocal
        UserHolder.saveUser(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal，防止内存泄漏
        UserHolder.removeUser();
    }

}
