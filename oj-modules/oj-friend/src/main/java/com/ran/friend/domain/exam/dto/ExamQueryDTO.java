package com.ran.friend.domain.exam.dto;

import com.ran.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    private String title;

    private String startTime;

    private String endTime;

    private Integer type; // 0 当前竞赛  1 历史竞赛  2 我的竞赛
}