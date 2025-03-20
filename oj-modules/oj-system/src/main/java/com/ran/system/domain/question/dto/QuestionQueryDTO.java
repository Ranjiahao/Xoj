package com.ran.system.domain.question.dto;

import com.ran.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {
    private Integer difficulty;
    private String title;
}