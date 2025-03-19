package com.ran.system.service.sysuser.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.constants.HttpConstants;
import com.ran.common.core.domain.LoginUser;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.core.enums.UserIdentity;
import com.ran.common.security.exception.ServiceException;
import com.ran.common.security.service.TokenService;
import com.ran.system.domain.sysuser.SysUser;
import com.ran.system.domain.sysuser.dto.SysUserSaveDTO;
import com.ran.system.mapper.sysuser.SysUserMapper;
import com.ran.system.service.sysuser.ISysUserService;
import com.ran.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public R<String> login(String userAccount, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getUserId, SysUser::getPassword, SysUser::getNickName)
                .eq(SysUser::getUserAccount, userAccount));
        if (sysUser == null) {
            // 用户不存在，未查询到
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (BCryptUtils.matchesPassword(password, sysUser.getPassword())) {
            // 密码正确，返回给用户token
            return R.ok(tokenService.createToken(sysUser.getUserId(),
                    secret, UserIdentity.ADMIN.getValue(), sysUser.getNickName(), null));
        }
        // 密码错误
        return R.fail(ResultCode.FAILED_LOGIN);
    }

    @Override
    public boolean logout(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return tokenService.deleteLoginUser(token, secret);
    }

    @Override
    public R<LoginUserVO> info(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if (loginUser == null) {
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        return R.ok(loginUserVO);
    }

    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        List<SysUser> sysUserList = sysUserMapper.selectList(queryWrapper
                .eq(SysUser::getUserAccount, sysUserSaveDTO.getUserAccount()));
        if (CollectionUtil.isNotEmpty(sysUserList)) {
            // 如果直接返回0，则无法显示详细错误信息
            throw new ServiceException(ResultCode.FAILED_USER_EXISTS);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(sysUserSaveDTO.getUserAccount());
        sysUser.setPassword(BCryptUtils.encryptPassword(sysUserSaveDTO.getPassword()));
        sysUser.setCreateBy(Constants.SYSTEM_USER_ID);
        return sysUserMapper.insert(sysUser);
    }
}