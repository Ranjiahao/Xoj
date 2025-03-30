package com.ran.friend.service.user;

import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;

public interface IUserExamService {

    int enter(Long examId);

    TableDataInfo list(ExamQueryDTO examQueryDTO);
}