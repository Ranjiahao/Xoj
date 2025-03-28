package com.ran.api.domain.vo;

import com.ran.api.domain.UserExeResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserQuestionResultVO {

    private Integer pass;      // 0未通过   1通过

    private String exeMessage; // 异常信息

    private List<UserExeResult> userExeResultList;

    @JsonIgnore
    private Integer score;
}