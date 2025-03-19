package com.ran.system.controller.sysuser;

import com.ran.common.core.constants.HttpConstants;
import com.ran.common.core.controller.BaseController;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.system.domain.sysuser.dto.LoginDTO;
import com.ran.system.domain.sysuser.dto.SysUserSaveDTO;
import com.ran.system.service.sysuser.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sysUser")
public class SysUserController extends BaseController {

    @Autowired
    private ISysUserService sysUserService;

    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDTO loginDTO) {
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return toR(sysUserService.logout(token));
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return sysUserService.info(token);
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody SysUserSaveDTO sysUserSaveDTO) {
        return toR(sysUserService.add(sysUserSaveDTO));
    }
}