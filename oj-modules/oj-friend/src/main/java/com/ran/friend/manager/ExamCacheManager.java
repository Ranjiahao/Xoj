package com.ran.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.enums.ExamListType;
import com.ran.common.core.enums.ResultCode;
import com.ran.common.redis.service.RedisService;
import com.ran.common.security.exception.ServiceException;
import com.ran.friend.domain.exam.Exam;
import com.ran.friend.domain.exam.ExamQuestion;
import com.ran.friend.domain.exam.dto.ExamQueryDTO;
import com.ran.friend.domain.exam.vo.ExamVO;
import com.ran.friend.domain.user.UserExam;
import com.ran.friend.mapper.exam.ExamMapper;
import com.ran.friend.mapper.exam.ExamQuestionMapper;
import com.ran.friend.mapper.user.UserExamMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ExamCacheManager {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 获取竞赛列表
     * @param examQueryDTO 竞赛查询条件
     * @param userId 若不为空，则返回用户参加的竞赛列表 <p>
     *               若为空，则根据examQueryDTO获得当前竞赛或者历史竞赛
     */
    public List<ExamVO> getExamVOList(ExamQueryDTO examQueryDTO, Long userId) {
        // 分页下标和redis下标统一
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1;
        String examListKey = getExamListKey(examQueryDTO.getType(), userId);
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            // 说明redis中数据可能有问题 从数据库中查数据并且重新刷新缓存
            examVOList = getExamListByDB(examQueryDTO, userId); // 从数据库中获取数据
            refreshCache(examQueryDTO.getType(), userId);
        }
        return examVOList;
    }

    public List<Long> getUserExamIdList(Long userId) {
        String examListKey = getUserExamListKey(userId);
        List<Long> userExamIdList = redisService.getCacheListByRange(examListKey, 0, -1, Long.class);
        if (CollectionUtil.isNotEmpty(userExamIdList)) {
            return userExamIdList;
        }
        // redis中没有数据，从数据库中查询
        List<UserExam> userExamList =
                userExamMapper.selectList(new LambdaQueryWrapper<UserExam>().eq(UserExam::getUserId, userId));
        if (CollectionUtil.isEmpty(userExamList)) {
            return null;
        }
        refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);
        return userExamList.stream().map(UserExam::getExamId).collect(Collectors.toList());
    }

    public Exam getExamDetail(Long examId) {
        String detailKey = getDetailKey(examId);
        Exam exam = redisService.getCacheObject(detailKey, Exam.class);
        if (exam != null) {
            return exam;
        }
        // redis中没有数据，从数据库中查询
        exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.EXAM_NOT_EXISTS);
        }
        redisService.setCacheObject(detailKey, exam);
        return exam;
    }

    public void addUserExamCache(Long userId, Long examId) {
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList(userExamListKey, examId);
    }

    public Long getFirstQuestion(Long examId) {
        return redisService.indexForList(getExamQuestionListKey(examId), 0, Long.class);
    }

    public Long preQuestion(Long examId, Long questionId) {
        Long index = redisService.indexOfForList(getExamQuestionListKey(examId), questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return redisService.indexForList(getExamQuestionListKey(examId), index - 1, Long.class);
    }

    public Long nextQuestion(Long examId, Long questionId) {
        Long index = redisService.indexOfForList(getExamQuestionListKey(examId), questionId);
        long lastIndex = getExamQuestionListSize(examId) - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return redisService.indexForList(getExamQuestionListKey(examId), index + 1, Long.class);
    }

    public void refreshCache(Integer examListType, Long userId) {
        List<Exam> examList = new ArrayList<>();
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            // 查询未完赛的竞赛列表
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .gt(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            // 查询历史竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .le(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.USER_EXAM_LIST.getValue().equals(examListType)) {
            List<ExamVO> examVOList = userExamMapper.selectUserExamList(userId);
            examList = BeanUtil.copyToList(examVOList, Exam.class);
        }
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);  // 刷新竞赛详情缓存
        redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(getExamListKey(examListType, userId), examIdList); // 刷新列表缓存
    }

    public void refreshExamQuestionCache(Long examId) {
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return;
        }
        List<Long> examQuestionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
        redisService.rightPushAll(getExamQuestionListKey(examId), examQuestionIdList);

        // 由于历史竞赛访问的人数少，所以设置过期时间为1天
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        redisService.expire(getExamQuestionListKey(examId), seconds, TimeUnit.SECONDS);
    }

    public Long getListSize(Integer examListType, Long userId) {
        String examListKey = getExamListKey(examListType, userId);
        return redisService.getListSize(examListKey);
    }

    public Long getExamQuestionListSize(Long examId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        return redisService.getListSize(examQuestionListKey);
    }

    private List<ExamVO> getExamListByDB(ExamQueryDTO examQueryDTO, Long userId) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        if (ExamListType.USER_EXAM_LIST.getValue().equals(examQueryDTO.getType())) {
            // 查询我的竞赛列表
            return userExamMapper.selectUserExamList(userId);
        } else {
            // 查询C端的竞赛列表
            return examMapper.selectExamList(examQueryDTO);
        }
    }

    /**
     * 根据竞赛id集合组装成ExamVO集合
     */
    private List<ExamVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            return null;
        }
        // 拼接redis当中key的方法 并且将拼接好的key存储到一个list中
        List<String> detailKeyList = new ArrayList<>();
        for (Long examId : examIdList) {
            detailKeyList.add(getDetailKey(examId));
        }
        // 批量获取redis当中的数据
        List<ExamVO> examVOList = redisService.multiGet(detailKeyList, ExamVO.class);
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            return null;
        }
        return examVOList;
    }

    private String getExamListKey(Integer examListType, Long userId) {
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_UNFINISHED_LIST;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_HISTORY_LIST;
        } else {
            return getUserExamListKey(userId);
        }
    }

    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST + userId;
    }

    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST + examId;
    }
}