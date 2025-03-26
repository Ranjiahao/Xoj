package com.ran.friend.service.question;

import com.ran.common.core.domain.TableDataInfo;
import com.ran.friend.domain.question.dto.QuestionQueryDTO;
import com.ran.friend.domain.question.vo.QuestionDetailVO;

public interface IQuestionService {

    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);
}