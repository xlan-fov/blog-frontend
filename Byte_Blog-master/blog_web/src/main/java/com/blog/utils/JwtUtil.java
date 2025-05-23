package com.blog.utils;


/*
 * @Author: 郭钰冉
 * @Date: 2025-5-6
 * @Description: Jwt工具类
 */

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.blog.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * @Author: guoyuran
 * @Date: 2025-5-6
 * @Description: Jwt加密验证工具类
 */

public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 密钥
     */
    private static final String SECRET = "s8W#bP2xN!k9VzR$uE6@1cGzQ*TdL3hY";

    /**
     * 过期时间
     **/
    private static final long EXPIRATION = 1800L;//单位为秒

    /**
     * 生成用户token,设置token超时时间
     */
    public static String generateToken(Users user) {
        //过期时间
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        // 当前时间戳，确保token唯一性
        long currentTimeMillis = System.currentTimeMillis();
        
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        
        String token = JWT.create()
                .withHeader(map)// 添加头部
                //可以将基本信息放到claims中
                .withClaim("id", user.getId()) //id
                .withClaim("userName", user.getUsername())//username
                .withClaim("phone", user.getPhone())
                .withClaim("iat_ms", currentTimeMillis) // 添加毫秒级时间戳确保唯一性
                .withExpiresAt(expireDate) //超时设置,设置过期的日期
                .withIssuedAt(new Date()) //签发时间
                .sign(Algorithm.HMAC256(SECRET)); //SECRET加密
                
        logger.info("生成token成功，用户: {}, 时间戳: {}", user.getUsername(), currentTimeMillis);
        return token;
    }

    /**
     * 校验token并解析token
     */
    public static Map<String, Claim> verifyToken(String token) {
        DecodedJWT jwt = null;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token);

            //decodedJWT.getClaim("属性").asString()  获取负载中的属性值

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("token解码异常");
            //解码异常则抛出异常
            return null;
        }
        return jwt.getClaims();
    }
}
