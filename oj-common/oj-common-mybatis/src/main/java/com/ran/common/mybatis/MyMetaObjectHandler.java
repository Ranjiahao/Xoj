package com.ran.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ran.common.core.constants.Constants;
import com.ran.common.core.utils.ThreadLocalUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // ThreadLocal获得当前调用接口的用户id
        this.strictInsertFill(metaObject, "createBy", Long.class, ThreadLocalUtil.get(Constants.USER_ID, Long.class));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateBy", ThreadLocalUtil.get(Constants.USER_ID, Long.class), metaObject);
    }
}
