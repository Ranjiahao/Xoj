package com.ran.system.service.exam.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.security.exception.ServiceException;
import com.ran.system.domain.exam.Exam;
import com.ran.system.domain.exam.ExamQuestion;
import com.ran.system.domain.exam.dto.ExamAddDTO;
import com.ran.system.domain.exam.dto.ExamEditDTO;
import com.ran.system.domain.exam.dto.ExamQueryDTO;
import com.ran.system.domain.exam.dto.ExamQuestAddDTO;
import com.ran.system.domain.exam.vo.ExamDetailVO;
import com.ran.system.domain.exam.vo.ExamVO;
import com.ran.system.domain.question.Question;
import com.ran.system.domain.question.vo.QuestionVO;
import com.ran.system.mapper.exam.ExamMapper;
import com.ran.system.mapper.exam.ExamQuestionMapper;
import com.ran.system.mapper.question.QuestionMapper;
import com.ran.system.service.exam.IExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public int add(ExamAddDTO examAddDTO) {
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        checkExamSaveParams(exam);
        return examMapper.insert(exam);
    }

    @Override
    public boolean questionAdd(ExamQuestAddDTO examQuestAddDTO) {
        // 竞赛存在且未开始、未发布
        Exam exam = getExam(examQuestAddDTO.getExamId());
        checkExamNotStarted(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        Set<Long> questionIdSet = examQuestAddDTO.getQuestionIdSet();
        if (CollectionUtil.isEmpty(questionIdSet)) {
            return true;
        }
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIdSet.size()) {
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }
        return saveExamQuestion(exam.getExamId(), questionIdSet);
    }

    @Override
    public int questionDelete(Long examId, Long questionId) {
        Exam exam = getExam(examId);
        checkExamNotStarted(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
    }

    @Override
    public ExamDetailVO detail(Long examId) {
        ExamDetailVO examDetailVO = new ExamDetailVO();
        Exam exam = getExam(examId);
        BeanUtil.copyProperties(exam, examDetailVO);
        List<QuestionVO> questionVOList = examQuestionMapper.selectExamQuestionList(examId);
        if (CollectionUtil.isEmpty(questionVOList)) {
            return examDetailVO;
        }
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        Exam exam = getExam(examEditDTO.getExamId());
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        checkExamNotStarted(exam);
        BeanUtil.copyProperties(examEditDTO, exam);
        checkExamSaveParams(exam);
        return examMapper.updateById(exam);
    }

    @Override
    public int delete(Long examId) {
        Exam exam = getExam(examId);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        checkExamNotStarted(exam);
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        return examMapper.deleteById(exam);
    }

    @Override
    public int publish(Long examId) {
        Exam exam = getExam(examId);
        if (exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }
        // select count(0) from tb_exam_question where exam_id = #{examId}
        Long count = examQuestionMapper
                .selectCount(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId));
        if (count == null || count <= 0) {
            throw new ServiceException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }
        exam.setStatus(Constants.TRUE);
        return examMapper.updateById(exam);
    }

    @Override
    public int cancelPublish(Long examId) {
        Exam exam = getExam(examId);
        checkExamNotStarted(exam);
        if (exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }
        exam.setStatus(Constants.FALSE);
        return examMapper.updateById(exam);
    }

    /**
     * 如果exam.examId为空，则校验添加竞赛请求<p>
     * 如果exam.examId不为空，则校验修改竞赛请求
     */
    private void checkExamSaveParams(Exam exam) {
        List<Exam> examList = examMapper
                .selectList(new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getTitle, exam.getTitle())
                        .ne(exam.getExamId() != null, Exam::getExamId, exam.getExamId()));
        // 标题不可重复
        if (CollectionUtil.isNotEmpty(examList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        // 竞赛开始时间不能早于当前时间
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        // 竞赛结束时间不能早已开始时间
        if (exam.getStartTime().isAfter(exam.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
    }

    /**
     * 校验竞赛是否已开始，如果竞赛已开始则抛出异常。
     */
    private void checkExamNotStarted(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }

    private boolean saveExamQuestion(Long examId, Set<Long> questionIdSet) {
        int num = 1;
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIdSet) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examId);
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }
        return saveBatch(examQuestionList);
    }

    private Exam getExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }
}