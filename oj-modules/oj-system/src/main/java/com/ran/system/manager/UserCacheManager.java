package com.ran.system.manager;

import com.ran.common.core.constants.CacheConstants;
import com.ran.common.redis.service.RedisService;
import com.ran.system.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RefreshScope
@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;

    /**
     * 用户详情缓存过期时间，默认10分钟
     */
    @Value("${friend.detail-expiration:10}")
    private Long userExp;

    public void updateStatus(Long userId, Integer status) {
        // 刷新用户缓存
        String userKey = getUserDetailKey(userId);
        User user = redisService.getCacheObject(userKey, User.class);
        if (user == null) {
            return;
        }
        user.setStatus(status);
        redisService.setCacheObject(userKey, user);
        // 设置用户缓存有效期为10分钟
        redisService.expire(userKey, userExp, TimeUnit.MINUTES);
    }

    private String getUserDetailKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}