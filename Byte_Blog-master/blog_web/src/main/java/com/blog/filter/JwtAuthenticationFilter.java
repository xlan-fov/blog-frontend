package com.blog.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import com.blog.dto.UserDTO;
import com.blog.utils.JwtUtil;
import com.blog.utils.RedisConstants;
import com.blog.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT认证过滤器 - 在请求开始时解析token并存储用户信息
 */
@Slf4j
@Component
@Order(1) // 确保在Spring Security过滤器之前执行
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    
    // 不需要验证的路径
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/users/registerByname",
            "/api/users/loginByname", 
            "/api/users/loginByphone",
            "/api/users/adminLogin",
            "/api/users/captcha",
            "/api/users/code",
            "/api/users/slider-image",
            "/api/users/slider-validate",
            "/api/health"
    );

    public JwtAuthenticationFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // 输出调试信息
        System.out.println("=== JWT过滤器处理请求: " + requestURI + " ===");
        log.info("JWT过滤器处理请求: {}", requestURI);
        
        // 检查是否需要跳过验证
        if (shouldSkip(requestURI)) {
            System.out.println("跳过JWT验证: " + requestURI);
            log.info("跳过JWT验证: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取并验证token
        String token = request.getHeader("Authorization");
        System.out.println("Authorization头: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "null"));
        
        if (StrUtil.isBlank(token)) {
            System.out.println("请求头中没有token，继续请求");
            log.warn("请求头中没有token");
            filterChain.doFilter(request, response);
            return;
        }

        // 验证token
        Map<String, Claim> claims = JwtUtil.verifyToken(token);
        if (claims == null) {
            System.out.println("Token验证失败，继续请求");
            log.warn("Token验证失败");
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println("Token验证成功，claims: " + claims.keySet());
        
        // 构建用户信息
        UserDTO userDTO = new UserDTO();
        
        try {
            // 从JWT中提取用户信息
            if (claims.containsKey("id")) {
                userDTO.setId(claims.get("id").asInt());
                System.out.println("从JWT提取用户ID: " + userDTO.getId());
            }
            
            if (claims.containsKey("userName")) {
                userDTO.setUsername(claims.get("userName").asString());
                System.out.println("从JWT提取用户名: " + userDTO.getUsername());
            }
            
            if (claims.containsKey("phone")) {
                userDTO.setPhone(claims.get("phone").asString());
            }
            
            // 设置默认值
            userDTO.setRole("user");
            userDTO.setIsBanned(0);
            userDTO.setIsLoggedIn(1);
            
            // 检查用户信息完整性
            if (userDTO.getId() == null || userDTO.getUsername() == null) {
                System.out.println("从JWT解析的用户信息不完整");
                log.warn("从JWT解析的用户信息不完整");
                
                // 尝试从Redis获取更完整的信息
                String redisKey = RedisConstants.LOGIN_USER_KEY + token;
                Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(redisKey);
                
                if (!userMap.isEmpty()) {
                    System.out.println("从Redis获取用户信息成功");
                    userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
                    // 刷新Redis有效期
                    stringRedisTemplate.expire(redisKey, 1800, TimeUnit.SECONDS);
                } else {
                    System.out.println("Redis中没有找到用户信息");
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                // 将JWT解析的信息存入Redis
                String redisKey = RedisConstants.LOGIN_USER_KEY + token;
                Map<String, String> userMap = new HashMap<>();
                userMap.put("id", String.valueOf(userDTO.getId()));
                userMap.put("username", userDTO.getUsername());
                if (userDTO.getPhone() != null) {
                    userMap.put("phone", userDTO.getPhone());
                }
                userMap.put("role", userDTO.getRole());
                
                stringRedisTemplate.opsForHash().putAll(redisKey, userMap);
                stringRedisTemplate.expire(redisKey, 1800, TimeUnit.SECONDS);
                System.out.println("用户信息已更新到Redis");
            }
            
        } catch (Exception e) {
            System.out.println("解析JWT失败: " + e.getMessage());
            log.error("解析JWT失败: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }

        // 保存用户信息到ThreadLocal
        System.out.println("保存用户信息到ThreadLocal: " + userDTO);
        UserHolder.saveUser(userDTO);
        
        try {
            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 确保在请求完成后清理ThreadLocal
            System.out.println("请求处理完成，清理ThreadLocal");
            UserHolder.removeUser();
        }
    }
    
    private boolean shouldSkip(String requestURI) {
        // 静态资源跳过验证
        if (requestURI.endsWith(".html") || 
            requestURI.endsWith(".js") || 
            requestURI.endsWith(".css") || 
            requestURI.endsWith(".ico") ||
            requestURI.startsWith("/static/")) {
            return true;
        }
        
        // 特定API路径跳过验证
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::contains);
    }
}
