package com.ran.friend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ran.friend.domain.user.UserSubmit;

import java.util.List;

public interface UserSubmitMapper extends BaseMapper<UserSubmit> {

    UserSubmit selectCurrentUserSubmit(Long userId, Long examId, Long questionId, String currentTime);

    List<Long> selectHostQuestionList();
}