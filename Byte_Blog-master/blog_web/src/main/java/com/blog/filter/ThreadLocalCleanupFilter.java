package com.blog.filter;

import com.blog.dto.UserDTO;
import com.blog.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用于清理ThreadLocal的过滤器 - 作为备用，确保ThreadLocal被清理
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE) // 确保最后执行
public class ThreadLocalCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        log.debug("ThreadLocalCleanupFilter处理请求: {}", requestURI);
        
        try {
            // 执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求处理完成后，确保ThreadLocal被清理
            UserDTO user = UserHolder.getUser();
            if (user != null) {
                log.debug("备用清理：请求结束，清理ThreadLocal中的用户: {}", user.getUsername());
                UserHolder.removeUser();
            }
        }
    }
}
