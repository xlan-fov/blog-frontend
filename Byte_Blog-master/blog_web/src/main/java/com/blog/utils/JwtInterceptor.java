package com.blog.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import com.blog.dto.UserDTO;
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

        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)){
            //    不存在，放行
            return true;
        }

        // 验证token
        Map<String, Claim> claims = JwtUtil.verifyToken(token);
        if (claims == null) {
            return true; // token无效，不拦截，让后面的拦截器判断
        }

        // 基于token获取redis用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);

        //3.判断用户是否存在
        if (userMap.isEmpty()) {
            //4.用户不存在，放行
            return true;
        }

        //5.将查询到的Hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 刷新有效期
        stringRedisTemplate.expire(key, 1800, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal，防止内存泄漏
        UserHolder.removeUser();
    }

}
