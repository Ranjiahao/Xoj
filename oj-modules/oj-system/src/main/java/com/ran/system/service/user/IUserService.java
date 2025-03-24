package com.ran.system.service.user;

import com.ran.system.domain.user.dto.UserDTO;
import com.ran.system.domain.user.dto.UserQueryDTO;
import com.ran.system.domain.user.vo.UserVO;

import java.util.List;

public interface IUserService {

    List<UserVO> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserDTO userDTO);
}