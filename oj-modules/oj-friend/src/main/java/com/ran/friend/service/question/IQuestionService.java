package com.ran.friend.service.question;

import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.domain.question.dto.QuestionQueryDTO;
import com.ran.friend.domain.question.vo.QuestionDetailVO;
import com.ran.friend.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {

    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    List<QuestionVO> hotList();

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);
}