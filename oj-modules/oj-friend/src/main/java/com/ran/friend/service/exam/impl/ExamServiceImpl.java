package com.ran.friend.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.domain.TableDataInfo;
import com.ran.common.core.utils.ThreadLocalUtil;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;
import com.ran.friend.domain.exam.vo.ExamVO;
import com.ran.friend.manager.ExamCacheManager;
import com.ran.friend.mapper.exam.ExamMapper;
import com.ran.friend.service.exam.IExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    @Override
    public TableDataInfo List(ExamQueryDTO examQueryDTO) {
        // 从redis中获取 竞赛列表数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        List<ExamVO> examVOList;
        if (total == null || total <= 0) {
            // redis当中没有数据，从数据库当中查询，并刷新redis
            PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
            examVOList = examMapper.selectExamList(examQueryDTO);
            total = new PageInfo<>(examVOList).getTotal();
            examCacheManager.refreshCache(examQueryDTO.getType(), null);
        } else {
            // 从redis当中获取
            examVOList = examCacheManager.getExamVOList(examQueryDTO, null);
            // 如果redis缓存出错，则可能从数据库中获取数据，并刷新缓存，total可能改变，所以需要重新获取
            total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        assembleExamVOList(examVOList);
        return TableDataInfo.success(examVOList, total);
    }

    @Override
    public String getFirstQuestion(Long examId) {
        checkAndRefresh(examId);
        return examCacheManager.getFirstQuestion(examId).toString();
    }

    @Override
    public String preQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        return examCacheManager.preQuestion(examId, questionId).toString();
    }

    @Override
    public String nextQuestion(Long examId, Long questionId) {
        checkAndRefresh(examId);
        return examCacheManager.nextQuestion(examId, questionId).toString();
    }

    /**
     * 统计用户是否报名
     */
    private void assembleExamVOList(List<ExamVO> examVOList) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        List<Long> userExamIdList = examCacheManager.getUserExamIdList(userId);
        if (CollectionUtil.isEmpty(userExamIdList)) {
            return;
        }
        for (ExamVO examVO : examVOList) {
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }

    private void checkAndRefresh(Long examId) {
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if (listSize == null || listSize <= 0) {
            examCacheManager.refreshExamQuestionCache(examId);
        }
    }
}