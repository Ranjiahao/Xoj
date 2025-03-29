package com.ran.friend.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSubmitDTO {

    /**
     * 如果不为空则表示竞赛中的题目，空则表示非竞赛状态下提交
     */
    private Long examId;

    private Long questionId;

    private Integer programType;

    private String userCode;
}