package com.vueones.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {

    @Value("${security.jwt.secret:}")
    private String secretBase64;

    @Value("${security.jwt.expiration:86400000}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secretBase64 != null && !secretBase64.trim().isEmpty()) {
            byte[] secretBytes = Base64.getDecoder().decode(secretBase64.trim());
            this.key = Keys.hmacShaKeyFor(secretBytes);
        } else {
            // 未配置密钥时，生成临时密钥（重启后会失效，不建议生产使用）
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
    }

    /**
     * 从令牌中获取用户ID
     * @param token 令牌
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        return Integer.valueOf(getClaimFromToken(token, Claims::getSubject));
    }

    /**
     * 从令牌中获取过期时间
     * @param token 令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取声明
     * @param token 令牌
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取所有声明
     * @param token 令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查令牌是否过期
     * @param token 令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为用户生成令牌
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 令牌
     */
    public String generateToken(Integer userId, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        return doGenerateToken(claims, userId.toString());
    }

    /**
     * 生成令牌
     * @param claims 声明
     * @param subject 主题
     * @return 令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 验证令牌
     * @param token 令牌
     * @param userId 用户ID
     * @return 验证结果
     */
    public Boolean validateToken(String token, Integer userId) {
        final Integer tokenUserId = getUserIdFromToken(token);
        return (tokenUserId.equals(userId) && !isTokenExpired(token));
    }
} 