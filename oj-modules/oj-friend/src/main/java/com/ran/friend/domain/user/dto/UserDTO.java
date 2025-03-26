package com.ran.friend.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号不合法")
    private String phone;

    private String code;
}