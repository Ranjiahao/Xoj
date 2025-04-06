package com.ran.friend.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.domain.LoginUser;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.core.enums.UserIdentity;
import com.ran.common.core.enums.UserStatus;
import com.ran.common.core.utils.ThreadLocalUtil;
import com.ran.common.message.service.AliSmsService;
import com.ran.common.redis.service.RedisService;
import com.ran.common.security.exception.ServiceException;
import com.ran.common.security.service.TokenService;
import com.ran.friend.domain.user.User;
import com.ran.friend.domain.user.dto.UserUpdateDTO;
import com.ran.friend.domain.user.vo.UserVO;
import com.ran.friend.manager.UserCacheManager;
import com.ran.friend.mapper.user.UserMapper;
import com.ran.friend.service.user.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@RefreshScope
@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AliSmsService aliSmsService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserCacheManager userCacheManager;

    // 验证码过期时间，默认5分钟
    @Value("${sms.code-expiration:5}")
    private Long phoneCodeExpiration;

    // 每天限制发送验证码次数，默认5次
    @Value("${sms.send-limit:5}")
    private Integer sendLimit;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${file.oss.downloadUrl}")
    private String downloadUrl;

    @Override
    public boolean sendCode(String phone) {
        // 以手机号为key在redis中查询
        String phoneCodeKey = getPhoneCodeKey(phone);
        Long expire = redisService.getExpire(phoneCodeKey, TimeUnit.SECONDS);
        if (expire != null && (phoneCodeExpiration * 60 - expire) < 60 ){
            // 60秒内不允许再发送验证码
            throw new ServiceException(ResultCode.FAILED_FREQUENT);
        }

        // 判断当天是否超过发送验证码次数
        String codeTimeKey = getCodeTimeKey(phone);
        Long sendTimes = redisService.getCacheObject(codeTimeKey, Long.class);
        if (sendTimes != null && sendTimes >= sendLimit) {
            // 超出当日限制发送验证码次数
            throw new ServiceException(ResultCode.FAILED_TIME_LIMIT);
        }

        // 随机生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 存储到redis    key：phone:code:手机号   value：code
        redisService.setCacheObject(phoneCodeKey, code, phoneCodeExpiration, TimeUnit.MINUTES);
        boolean sendMobileCode = aliSmsService.sendMobileCode(phone, code);
        if (!sendMobileCode) {
            throw new ServiceException(ResultCode.FAILED_SEND_CODE);
        }
        redisService.increment(codeTimeKey);
        if (sendTimes == null) {
            // 当天第一次发起获取验证码的请求，设置缓存过期时间为当日23:59:59
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(codeTimeKey, seconds, TimeUnit.SECONDS);
        }
        return true;
    }

    @Override
    public String codeLogin(String phone, String code) {
        checkCode(phone, code);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            // 如果为新用户则注册
            user = new User();
            user.setPhone(phone);
            user.setStatus(UserStatus.Normal.getValue());
            user.setCreateBy(Constants.SYSTEM_USER_ID);
            userMapper.insert(user);
        }
        return tokenService.createToken(user.getUserId(), secret, UserIdentity.ORDINARY.getValue(), user.getNickName(), user.getHeadImage());
    }

    @Override
    public boolean logout() {
        String userKey = getCurUserKey();
        return tokenService.deleteLoginUser(userKey);
    }

    @Override
    public R<LoginUserVO> info() {
        String userKey = getCurUserKey();
        LoginUser loginUser = tokenService.getLoginUser(userKey);
        if (loginUser == null) {
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        if (StrUtil.isNotEmpty(loginUser.getHeadImage())) {
            loginUserVO.setHeadImage(downloadUrl + loginUser.getHeadImage());
        }
        return R.ok(loginUserVO);
    }

    @Override
    public UserVO detail() {
        Long userId = getCurUserId();
        UserVO userVO = userCacheManager.getUserById(userId);
        if (userVO == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (StrUtil.isNotEmpty(userVO.getHeadImage())) {
            userVO.setHeadImage(downloadUrl + userVO.getHeadImage());
        }
        return userVO;
    }

    @Override
    public int edit(UserUpdateDTO userUpdateDTO) {
        User user = getCurUser();
        BeanUtil.copyProperties(userUpdateDTO, user);
        // 更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY, String.class));
        return userMapper.updateById(user);
    }

    @Override
    public int updateHeadImage(String headImage) {
        User user = getCurUser();
        user.setHeadImage(headImage);
        // 更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(),user.getHeadImage(),
                ThreadLocalUtil.get(Constants.USER_KEY, String.class));
        return userMapper.updateById(user);
    }

    /**
     * 通过threadlocal获得当前用户userKey
     */
    private static @NotNull String getCurUserKey() {
        String userKey = ThreadLocalUtil.get(Constants.USER_KEY, String.class);
        if (userKey == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        return userKey;
    }

    /**
     * 通过threadlocal获得当前用户id
     */
    private static @NotNull Long getCurUserId() {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        return userId;
    }

    /**
     * 从数据库获取当前用户所有信息
     */
    private @NotNull User getCurUser() {
        Long userId = getCurUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 校验验证码
     */
    private void checkCode(String phone, String code) {
        String phoneCodeKey = getPhoneCodeKey(phone);
        if ("17791283068".equals(phone) && "123456".equals(code)) {
            // 测试账号，直接通过
            redisService.deleteObject(phoneCodeKey);
            return;
        }
        String cacheCode = redisService.getCacheObject(phoneCodeKey, String.class);
        if (StrUtil.isEmpty(cacheCode)) {
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        if (!cacheCode.equals(code)) {
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        // 验证码比对成功
        redisService.deleteObject(phoneCodeKey);
    }

    private String getPhoneCodeKey(String phone) {
        return CacheConstants.PHONE_CODE_KEY + phone;
    }

    private String getCodeTimeKey(String phone) {
        return CacheConstants.CODE_TIMES_KEY + phone;
    }
}