package com.ran.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode {
    SUCCESS                     (1000, "操作成功"),
    ERROR                       (2000, "服务繁忙请稍后重试"),
    FAILED                      (3000, "操作失败"),
    FAILED_UNAUTHORIZED         (3001, "未授权"),
    FAILED_PARAMS_VALIDATE      (3002, "参数校验失败"),
    FAILED_NOT_EXISTS           (3003, "资源不存在"),
    FAILED_ALREADY_EXISTS       (3004, "资源已存在"),
    AILED_USER_EXISTS           (3101, "用户已存在"),
    FAILED_USER_NOT_EXISTS      (3102, "用户不存在"),
    FAILED_LOGIN                (3103, "账号或密码错误"),
    FAILED_USER_BANNED          (3104, "您已被列入黑名单, 请联系管理员."),
    FAILED_USER_PHONE           (3105, "你输入的手机号有误"),
    FAILED_FREQUENT             (3106, "操作频繁，请稍后重试"),
    FAILED_TIME_LIMIT           (3107, "当天请求次数已达到上限"),
    FAILED_SEND_CODE            (3108, "验证码发送错误"),
    FAILED_INVALID_CODE         (3109, "验证码无效"),
    FAILED_ERROR_CODE           (3110, "验证码错误");
    private final int code;
    private final String msg;
}
