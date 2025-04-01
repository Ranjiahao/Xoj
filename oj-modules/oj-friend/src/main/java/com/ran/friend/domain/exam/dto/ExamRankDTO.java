package com.ran.friend.domain.exam.dto;

import com.ran.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRankDTO extends PageQueryDTO {

    private Long examId;
}