package com.ran.friend.service.exam;

import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;

public interface IExamService {

    TableDataInfo List(ExamQueryDTO examQueryDTO);

    String getFirstQuestion(Long examId);

    String preQuestion(Long examId, Long questionId);

    String nextQuestion(Long examId, Long questionId);
}