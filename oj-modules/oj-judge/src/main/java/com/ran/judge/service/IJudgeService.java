package com.ran.judge.service;

import com.ran.api.domain.dto.JudgeSubmitDTO;
import com.ran.api.domain.vo.UserQuestionResultVO;

public interface IJudgeService {
    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}