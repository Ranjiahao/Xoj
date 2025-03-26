package com.ran.friend.service.user;

import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.friend.domain.user.dto.UserUpdateDTO;
import com.ran.friend.domain.user.vo.UserVO;

public interface IUserService {
    boolean sendCode(String phone);

    String codeLogin(String phone, String code);

    boolean logout();

    R<LoginUserVO> info();

    UserVO detail();

    int edit(UserUpdateDTO userUpdateDTO);

    int updateHeadImage(String headImage);
}