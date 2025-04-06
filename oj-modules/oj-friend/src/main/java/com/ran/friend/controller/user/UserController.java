package com.ran.friend.controller.user;

import com.ran.common.core.controller.BaseController;
import com.ran.common.core.domain.R;
import com.ran.common.core.domain.vo.LoginUserVO;
import com.ran.friend.domain.user.dto.UserDTO;
import com.ran.friend.domain.user.dto.UserUpdateDTO;
import com.ran.friend.domain.user.vo.UserVO;
import com.ran.friend.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    @PostMapping("/sendCode")
    public R<Void> sendCode(@RequestBody @Validated UserDTO userDTO) {
        return toR(userService.sendCode(userDTO.getPhone()));
    }

    @PostMapping("/code/login")
    public R<String> codeLogin(@RequestBody @Validated UserDTO userDTO) {
        return R.ok(userService.codeLogin(userDTO.getPhone(), userDTO.getCode()));
    }

    @DeleteMapping("/logout")
    public R<Void> logout() {
        return toR(userService.logout());
    }

    @GetMapping("/info")
    public R<LoginUserVO> info() {
        return userService.info();
    }

    @GetMapping("/detail")
    public R<UserVO> detail() {
        return R.ok(userService.detail());
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody UserUpdateDTO userUpdateDTO) {
        return toR(userService.edit(userUpdateDTO));
    }

    @PutMapping("/head-image/update")
    public R<Void> updateHeadImage(@RequestBody UserUpdateDTO userUpdateDTO) {
        return toR(userService.updateHeadImage(userUpdateDTO.getHeadImage()));
    }
}