package com.ran.job.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ran.common.core.constants.CacheConstants;
import com.ran.common.core.constants.Constants;
import com.ran.common.redis.service.RedisService;
import com.ran.job.domain.exam.Exam;
import com.ran.job.domain.message.Message;
import com.ran.job.domain.message.MessageText;
import com.ran.job.domain.user.UserScore;
import com.ran.job.mapper.exam.ExamMapper;
import com.ran.job.mapper.user.UserExamMapper;
import com.ran.job.mapper.user.UserSubmitMapper;
import com.ran.job.service.IMessageService;
import com.ran.job.service.IMessageTextService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExamXxlJob {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IMessageTextService messageTextService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private UserExamMapper userExamMapper;


    @XxlJob("examResultHandler")
    public void examResultHandler() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusDateTime = now.minusDays(1);
        List<Exam> examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle)
                .eq(Exam::getStatus, Constants.TRUE)
                .ge(Exam::getEndTime, minusDateTime)
                .le(Exam::getEndTime, now));
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }
        Set<Long> examIdSet = examList.stream().map(Exam::getExamId).collect(Collectors.toSet());
        List<UserScore> userScoreList = userSubmitMapper.selectUserScoreList(examIdSet);
        Map<Long, List<UserScore>> userScoreMap = userScoreList.stream().collect(Collectors.groupingBy(UserScore::getExamId));
        createMessage(examList, userScoreMap);
    }

    private void createMessage(List<Exam> examList, Map<Long, List<UserScore>> userScoreMap) {
        // 批量插入tb_message_text表和tb_message表的数据结构
        List<MessageText> messageTextList = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();

        for (Exam exam : examList) {
            Long examId = exam.getExamId();
            List<UserScore> userScoreList = userScoreMap.get(examId);
            int totalUser = userScoreList.size();
            int examRank = 1;
            for (UserScore userScore : userScoreList) {
                // 组织通知信息
                String msgTitle =  exam.getTitle() + "——排名情况";
                String msgContent = "您所参与的竞赛：" + exam.getTitle()
                        + "，本次参与竞赛一共" + totalUser + "人， 您排名第"  + examRank + "名！";
                userScore.setExamRank(examRank);
                examRank++;

                // 插入tb_message_text表的数据
                MessageText messageText = new MessageText();
                messageText.setMessageTitle(msgTitle);
                messageText.setMessageContent(msgContent);
                messageText.setCreateBy(Constants.SYSTEM_USER_ID);
                messageTextList.add(messageText);

                // 插入tb_message表的数据
                Message message = new Message();
                message.setSendId(Constants.SYSTEM_USER_ID);
                message.setCreateBy(Constants.SYSTEM_USER_ID);
                message.setRecId(userScore.getUserId());
                messageList.add(message);
            }
            // 更新用户竞赛表
            userExamMapper.updateUserScoreAndRank(userScoreList);
            // 将竞赛排名信息刷入redis
            redisService.rightPushAll(getExamRankListKey(examId), userScoreList);
        }

        // 批量插入tb_message_text表
        messageTextService.batchInsert(messageTextList);

        Map<String, MessageText> messageTextMap = new HashMap<>();
        for (int i = 0; i < messageTextList.size(); i++) {
            MessageText messageText = messageTextList.get(i);
            String msgDetailKey = getMsgDetailKey(messageText.getTextId());
            messageTextMap.put(msgDetailKey, messageText);
            // 只有先插入tb_message_text表，才能获取到雪花算法生成的textId
            Message message = messageList.get(i);
            message.setTextId(messageText.getTextId());
        }
        // 批量插入tb_message表
        messageService.batchInsert(messageList);

        // 将消息详情放入redis中
        redisService.multiSet(messageTextMap);

        // 将用户消息列表放入redis中
        messageList.stream()
                .collect(Collectors.groupingBy(Message::getRecId))
                .forEach((recId, messages) -> {
                    String userMsgListKey = getUserMsgListKey(recId);
                    List<Long> userMsgTextIdList = messages.stream()
                            .map(Message::getTextId)
                            .toList();
                    redisService.rightPushAll(userMsgListKey, userMsgTextIdList);
                });
    }

    private String getUserMsgListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST + userId;
    }

    private String getMsgDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL + textId;
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST + examId;
    }
}