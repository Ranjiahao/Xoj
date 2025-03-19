package com.ran.system.service.sysuser;

import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.system.domain.sysuser.dto.SysUserSaveDTO;

public interface ISysUserService {

    R<String> login(String userAccount, String password);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

    int add(SysUserSaveDTO sysUserSaveDTO);
}
