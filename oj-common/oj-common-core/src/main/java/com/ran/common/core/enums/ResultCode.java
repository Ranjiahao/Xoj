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
    FAILED_USER_EXISTS          (3101, "用户已存在"),
    FAILED_USER_NOT_EXISTS      (3102, "用户不存在"),
    FAILED_LOGIN                (3103, "账号或密码错误"),

    EXAM_START_TIME_BEFORE_CURRENT_TIME (3201, "竞赛开始时间不能早于当前时间"),
    EXAM_START_TIME_AFTER_END_TIME      (3202, "竞赛开始时间不能晚于竞赛结束时间"),
    EXAM_NOT_EXISTS                     (3203, "竞赛不存在"),
    EXAM_QUESTION_NOT_EXISTS            (3204, "为竞赛新增的题目不存在"),
    EXAM_STARTED                        (3205, "竞赛已经开始，无法进行操作"),
    EXAM_NOT_HAS_QUESTION               (3206, "竞赛当中不包含题目"),
    EXAM_IS_FINISH                      (3207, "竞赛已经结束不能进行操作"),
    EXAM_IS_PUBLISH                     (3208, "竞赛已经发布不能进行编辑、删除操作");

    private final int code;
    private final String msg;
}
