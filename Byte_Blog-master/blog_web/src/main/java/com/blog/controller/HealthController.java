package com.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查数据库连接
        try {
            Integer dbResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("database", dbResult != null && dbResult == 1 ? "OK" : "ERROR");
        } catch (Exception e) {
            result.put("database", "ERROR: " + e.getMessage());
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().get("health.check");
            result.put("redis", "OK");
        } catch (Exception e) {
            result.put("redis", "ERROR: " + e.getMessage());
        }
        
        return result;
    }
}
