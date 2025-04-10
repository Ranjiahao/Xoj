package com.ran.friend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.domain.TableDataInfo;
import com.ran.common.core.enums.ExamListType;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.core.utils.ThreadLocalUtil;
import com.ran.common.security.exception.ServiceException;
import com.ran.friend.domain.exam.Exam;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;
import com.ran.friend.domain.exam.vo.ExamVO;
import com.ran.friend.domain.user.UserExam;
import com.ran.friend.manager.ExamCacheManager;
import com.ran.friend.mapper.user.UserExamMapper;
import com.ran.friend.service.user.IUserExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserExamServiceImpl implements IUserExamService {

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    @Override
    public int enter(Long examId) {
        Exam exam = examCacheManager.getExamDetail(examId);
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        List<Long> examIds = examCacheManager.getUserExamIdList(userId);
        if (examIds != null) {
            examIds.forEach(id -> {
                if (id != null && id.equals(examId)) {
                    throw new ServiceException(ResultCode.USER_EXAM_HAS_ENTER);
                }
            });
        }
        examCacheManager.addUserExamCache(userId, examId);
        UserExam userExam = new UserExam();
        userExam.setExamId(examId);
        userExam.setUserId(userId);
        return userExamMapper.insert(userExam);
    }

    @Override
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        examQueryDTO.setType(ExamListType.USER_EXAM_LIST.getValue());
        Long total = examCacheManager.getListSize(ExamListType.USER_EXAM_LIST.getValue(), userId);
        List<ExamVO> examVOList;
        if (total == null || total <= 0) {
            // 从数据库中查询我的竞赛列表
            PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
            examVOList = userExamMapper.selectUserExamList(userId);
            examCacheManager.refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            examVOList = examCacheManager.getExamVOList(examQueryDTO, userId);
            total = examCacheManager.getListSize(examQueryDTO.getType(), userId);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(examVOList, total);
    }
}