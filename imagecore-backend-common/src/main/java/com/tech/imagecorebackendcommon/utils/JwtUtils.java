package com.tech.imagecorebackendcommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtils {
    /**
     * JWT密钥
     */
    public static final String JWT_SECRET = "imagocore-backend-user-service-jwt-secret-key";

    /**
     * JWT过期时间
     */
    public static final long JWT_EXPIRATION = 3 * 24 * 60 * 60 * 1000L;

    /**
     * JWT令牌前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT请求头
     */
    public static final String JWT_HEADER = "Authorization";

    /**
     * 生成JWT令牌
     *
     * @param claims 用户信息
     * @param secretKey JWT密钥
     * @return JWT令牌
     */
    public static String generateToken(Map<String, Object> claims, String subject, SecretKey secretKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token, SecretKey secretKey) {
        Claims claims = getClaimsFromToken(token, secretKey);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从JWT令牌中获取用户账号
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @return 用户账号
     */
    public static String getUserAccountFromToken(String token, SecretKey secretKey) {
        Claims claims = getClaimsFromToken(token, secretKey);
        return claims.get("userAccount").toString();
    }

    /**
     * 从JWT令牌中获取用户角色
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @return 用户角色
     */
    public static String getUserRoleFromToken(String token, SecretKey secretKey) {
        Claims claims = getClaimsFromToken(token, secretKey);
        return claims.get("userRole").toString();
    }

    /**
     * 从JWT令牌中获取指定键的值
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @param key 键
     * @return 值
     */
    public static Object getClaimFromToken(String token, SecretKey secretKey, String key) {
        Claims claims = getClaimsFromToken(token, secretKey);
        return claims.get(key);
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, SecretKey secretKey) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT令牌中获取Claims
     *
     * @param token JWT令牌
     * @param secretKey JWT密钥
     * @return Claims
     */
    private static Claims getClaimsFromToken(String token, SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 创建JWT密钥
     */
    public static SecretKey createSecretKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

}