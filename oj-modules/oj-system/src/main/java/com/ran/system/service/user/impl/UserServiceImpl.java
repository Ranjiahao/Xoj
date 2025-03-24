package com.ran.system.service.user.impl;

import com.github.pagehelper.PageHelper;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.security.exception.ServiceException;
import com.ran.system.domain.user.User;
import com.ran.system.domain.user.dto.UserDTO;
import com.ran.system.domain.user.dto.UserQueryDTO;
import com.ran.system.domain.user.vo.UserVO;
import com.ran.system.mapper.user.UserMapper;
import com.ran.system.service.user.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserDTO userDTO) {
        User user = userMapper.selectById(userDTO.getUserId());
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userDTO.getStatus());
        return userMapper.updateById(user);
    }
}