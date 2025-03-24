package com.ran.system.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ran.system.domain.user.User;
import com.ran.system.domain.user.dto.UserQueryDTO;
import com.ran.system.domain.user.vo.UserVO;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    List<UserVO> selectUserList(UserQueryDTO userQueryDTO);
}
