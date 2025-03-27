package com.ran.friend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ran.friend.domain.exam.vo.ExamRankVO;
import com.ran.friend.domain.exam.vo.ExamVO;
import com.ran.friend.domain.user.UserExam;

import java.util.List;


public interface UserExamMapper extends BaseMapper<UserExam> {

    List<ExamVO> selectUserExamList(Long userId);
}