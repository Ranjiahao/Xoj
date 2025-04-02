package com.ran.job.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.core.constants.Constants;
import com.ran.common.redis.service.RedisService;
import com.ran.job.mapper.user.UserSubmitMapper;
import com.github.pagehelper.PageHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class QuestionXxlJob {

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RedisService redisService;

    @XxlJob("hostQuestionListHandler")
    public void hostQuestionListHandler() {
        log.info("----- 题目热门列表统计开始 ------");
        PageHelper.startPage(Constants.HOST_QUESTION_LIST_START, Constants.HOST_QUESTION_LIST_END);
        List<Long> questionIdList = userSubmitMapper.selectHostQuestionList();
        if (CollectionUtil.isEmpty(questionIdList)) {
            return;
        }
        redisService.deleteObject(CacheConstants.QUESTION_HOST_LIST);
        redisService.rightPushAll(CacheConstants.QUESTION_HOST_LIST, questionIdList);
        log.info("----- 题目热门列表统计结束 ------");
    }
}