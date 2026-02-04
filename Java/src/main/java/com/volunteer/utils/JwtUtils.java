package com.volunteer.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtils {

    // 密钥，生产环境应该配置在配置文件中，这里简单生成一个
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token 过期时间，这里设置稍微长一点，实际过期由 Redis 控制
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L; 

    /**
     * 生成 Token
     * @param claims 载荷数据
     * @return Token 字符串
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token
     * @param token Token 字符串
     * @return Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 Token 是否有效（只校验格式和签名，业务有效性查 Redis）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
