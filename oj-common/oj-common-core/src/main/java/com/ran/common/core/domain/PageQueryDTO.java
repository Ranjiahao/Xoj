package com.ran.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQueryDTO {

    private Integer pageSize = 10;

    private Integer pageNum = 1;
}
