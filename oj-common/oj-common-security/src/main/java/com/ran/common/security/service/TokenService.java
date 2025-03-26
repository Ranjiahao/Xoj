package com.ran.common.security.service;

import cn.hutool.core.lang.UUID;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.core.constants.JwtConstants;
import com.ran.common.redis.service.RedisService;
import com.ran.common.core.domain.LoginUser;
import com.ran.common.core.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private RedisService redisService;

    /**
     * 登录过期时间，默认720分钟
     */
    @Value("${login.expiration:720}")
    private Long exp;

    /**
     * 刷新缓存时间阈值，默认100分钟
     */
    @Value("${login.expiration:100}")
    private Long refreshTime;

    public String createToken(Long userId, String secret, Integer identity, String nickName, String headImage) {
        String userKey = UUID.fastUUID().toString();

        // 通过userId+uuid生成token并返回给用户
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claims, secret);

        // k: 前缀+uuid v: 用户身份、昵称、头像 放入Redis
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        redisService.setCacheObject(tokenKey, loginUser, exp, TimeUnit.MINUTES);

        return token;
    }

    /**
     * 在身份认证通过之后才会调用的，并且在请求到达controller层之前在拦截器中调用
     */
    public void extendToken(Claims claims) {
        String userKey = getUserKey(claims);
        if (userKey == null) {
            return;
        }
        String tokenKey = getTokenKey(userKey);
        Long expire = redisService.getExpire(tokenKey, TimeUnit.MINUTES);
        if (expire != null && expire < refreshTime) {
            redisService.expire(tokenKey, exp, TimeUnit.MINUTES);
        }
    }

    public LoginUser getLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    public LoginUser getLoginUser(String userKey) {
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    public boolean deleteLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }

    public boolean deleteLoginUser(String userKey) {
        return redisService.deleteObject(getTokenKey(userKey));
    }

    public Long getUserId(Claims claims) {
        if (claims == null) return null;
        return Long.valueOf(JwtUtils.getUserId(claims));
    }

    public String getUserKey(Claims claims) {
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);
    }

    private String getUserKey(String token, String secret) {
        Claims claims = getClaims(token, secret);
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);
    }

    public Claims getClaims(String token, String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret);
            if (claims == null) {
                log.error("解析token：{}, 出现异常", token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token：{}, 出现异常", token, e);
            return null;
        }
        return claims;
    }

    public void refreshLoginUser(String nickName, String headImage, String userKey) {
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = redisService.getCacheObject(tokenKey, LoginUser.class);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        redisService.setCacheObject(tokenKey, loginUser);
    }

    private String getTokenKey(String userKey) {
        return CacheConstants.LOGIN_TOKEN_KEY + userKey;
    }
}