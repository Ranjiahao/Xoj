package com.ran.system.service.exam;

import com.ran.system.domain.exam.dto.ExamAddDTO;
import com.ran.system.domain.exam.dto.ExamEditDTO;
import com.ran.system.domain.exam.dto.ExamQueryDTO;
import com.ran.system.domain.exam.dto.ExamQuestAddDTO;
import com.ran.system.domain.exam.vo.ExamDetailVO;
import com.ran.system.domain.exam.vo.ExamVO;

import java.util.List;

public interface IExamService {

    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    String add(ExamAddDTO examAddDTO);

    boolean questionAdd(ExamQuestAddDTO examQuestAddDTO);

    int questionDelete(Long examId, Long questionId);

    ExamDetailVO detail(Long examId);

    int edit(ExamEditDTO examEditDTO);

    int delete(Long examId);

    int publish(Long examId);

    int cancelPublish(Long examId);
}