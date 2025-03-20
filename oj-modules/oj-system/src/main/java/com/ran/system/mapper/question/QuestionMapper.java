package com.ran.system.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ran.system.domain.question.Question;
import com.ran.system.domain.question.dto.QuestionQueryDTO;
import com.ran.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {

    List<QuestionVO> selectQuestionList(QuestionQueryDTO questionQueryDTO);
}