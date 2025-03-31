package com.ran.friend.service.user;

import com.ran.common.core.domain.PageQueryDTO;
import com.ran.common.core.domain.TableDataInfo;

public interface IUserMessageService {
    TableDataInfo list(PageQueryDTO dto);
}