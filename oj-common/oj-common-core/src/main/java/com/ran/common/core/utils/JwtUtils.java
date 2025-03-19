package com.ran.common.core.utils;

import com.ran.common.core.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Map;

public class JwtUtils {
    /**
     * 生成令牌
     *
     * @param claims 数据
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claims(claims)
                .signWith(key)   // 默认使用HS256
                .compact();
    }

    /**
     * 从令牌中获取数据
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String secret) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("JWT令牌解析失败", e);
        }
    }

    public static String getUserKey(Claims claims) {
        return toStr(claims.get(JwtConstants.LOGIN_USER_KEY));
    }

    public static String getUserId(Claims claims) {
        return toStr(claims.get(JwtConstants.LOGIN_USER_ID));
    }

    private static String toStr(Object value) {
        return value == null ? "" : value.toString();
    }
}
