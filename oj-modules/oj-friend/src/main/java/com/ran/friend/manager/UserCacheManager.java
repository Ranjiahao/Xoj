package com.ran.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.redis.service.RedisService;
import com.ran.friend.domain.user.User;
import com.ran.friend.domain.user.vo.UserVO;
import com.ran.friend.mapper.user.UserMapper;
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

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户详情缓存过期时间，默认10分钟
     */
    @Value("${friend.detail-expiration:10}")
    private Long userExp;

    public UserVO getUserById(Long userId) {
        String userDetailKey = getUserDetailKey(userId);
        UserVO userVO = redisService.getCacheObject(userDetailKey, UserVO.class);
        if (userVO != null) {
            // 将缓存延长10min
            redisService.expire(userDetailKey, userExp, TimeUnit.MINUTES);
            return userVO;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId,
                        User::getNickName,
                        User::getHeadImage,
                        User::getSex,
                        User::getEmail,
                        User::getPhone,
                        User::getWechat,
                        User::getIntroduce,
                        User::getSchoolName,
                        User::getMajorName,
                        User::getStatus)
                .eq(User::getUserId, userId));
        if (user == null) {
            return null;
        }
        refreshUser(user);
        userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public void refreshUser(User user) {
        // 刷新用户缓存
        String userKey = getUserDetailKey(user.getUserId());
        redisService.setCacheObject(userKey, user);
        // 设置用户缓存有效期为10分钟
        redisService.expire(userKey, userExp, TimeUnit.MINUTES);
    }

    private String getUserDetailKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}