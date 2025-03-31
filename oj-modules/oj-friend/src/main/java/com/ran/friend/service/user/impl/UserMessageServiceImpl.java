package com.ran.friend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.domain.PageQueryDTO;
import com.ran.common.core.domain.TableDataInfo;
import com.ran.common.core.utils.ThreadLocalUtil;
import com.ran.friend.domain.message.vo.MessageTextVO;
import com.ran.friend.manager.MessageCacheManager;
import com.ran.friend.mapper.message.MessageTextMapper;
import com.ran.friend.service.user.IUserMessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserMessageServiceImpl implements IUserMessageService {

    @Autowired
    private MessageCacheManager messageCacheManager;

    @Autowired
    private MessageTextMapper messageTextMapper;

    @Override
    public TableDataInfo list(PageQueryDTO dto) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        Long total = messageCacheManager.getListSize(userId);
        List<MessageTextVO> messageTextVOList;
        if (total == null || total <= 0) {
            // 从数据库中查询我的消息列表
            PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
            messageTextVOList = messageTextMapper.selectUserMsgList(userId);
            messageCacheManager.refreshCache(userId);
            total = new PageInfo<>(messageTextVOList).getTotal();
        } else {
            messageTextVOList = messageCacheManager.getMsgTextVOList(dto, userId);
            // 如果redis缓存出错，则可能从数据库中获取数据，并刷新缓存，total可能改变，所以需要重新获取
            total = messageCacheManager.getListSize(userId);
        }
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(messageTextVOList, total);
    }
}