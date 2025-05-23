package com.blog.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import com.blog.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.blog.utils.RedisConstants.LOGIN_USER_KEY;

/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: Jwt拦截器
 */

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    //手动定义的需要手动注入spring容器中
    private StringRedisTemplate stringRedisTemplate;

    //通过构造方法注入
    public JwtInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("JWT拦截器开始处理请求: {}", request.getRequestURI());
        
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)){
            log.debug("请求头中没有token");
            return true;
        }

        log.debug("收到token: {}", token.substring(0, Math.min(20, token.length())) + "...");

        // 验证token
        Map<String, Claim> claims = JwtUtil.verifyToken(token);
        if (claims == null) {
            log.warn("Token验证失败");
            return true;
        }

        log.debug("Token验证成功，解析出的claims: {}", claims.keySet());

        // 基于token获取redis用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);

        if (userMap.isEmpty()) {
            //log.warn("Redis中未找到用户信息，key: {}", key);
            //
            //// 如果Redis中没有用户信息，尝试从JWT claims中构建用户信息
            //UserDTO userDTO = new UserDTO();
            //if (claims.containsKey("id")) {
            //    userDTO.setId(claims.get("id").asInt());
            //}
            //if (claims.containsKey("userName")) {
            //    userDTO.setUsername(claims.get("userName").asString());
            //}
            //if (claims.containsKey("phone")) {
            //    userDTO.setPhone(claims.get("phone").asString());
            //}
            //
            //log.debug("从JWT claims构建用户信息: {}", userDTO);
            //UserHolder.saveUser(userDTO);
            return true;
        }

        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        log.debug("从Redis获取用户信息: {}", userDTO);

        // 保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 刷新有效期
        stringRedisTemplate.expire(key, 1800, TimeUnit.SECONDS);

        log.debug("用户信息已设置到ThreadLocal");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal，防止内存泄漏
        UserHolder.removeUser();
        log.debug("已清理ThreadLocal用户信息");
    }

}
