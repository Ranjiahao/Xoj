package com.ran.friend.service.user;

import com.ran.api.domain.vo.UserQuestionResultVO;
import com.ran.friend.domain.user.dto.UserSubmitDTO;

public interface IUserQuestionService {

    boolean submit(UserSubmitDTO submitDTO);

    UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime);
}