package com.jd.workflow.console.config.dao;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;


import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * 自动填充功能
 *
 * @author wangjingfang3
 * @create 2021-07-28
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    public MyMetaObjectHandler(){
    }
    private String getClassName(MetaObject metaObject){
        if(metaObject.getOriginalObject()!=null){
            return metaObject.getOriginalObject().getClass().getSimpleName();
        }
        return null;
    }
    @Override
    public void insertFill(MetaObject metaObject) {
        if(MetaContextHelper.isSkipModify()!=null
                && MetaContextHelper.isSkipModify()
        ) return;

        log.info("insert fill"+getClassName(metaObject));
        if (metaObject.hasSetter("created")) {
            Object created = this.getFieldValByName("created", metaObject);
            if(ObjectHelper.isEmpty(created)){
                this.strictInsertFill(metaObject, "created", Date.class, new Date());
            }

        }
        Optional<UserInfoInSession> userOpt = Optional.ofNullable(UserSessionLocal.getUser());
        if (metaObject.hasSetter("creator")) {
            // this.strictInsertFill(metaObject, "creatorId", String.class, userOpt.orElse(new UserInfoInSession()).getUserId());
            this.fillStrategy(metaObject, "creator", userOpt.orElse(new UserInfoInSession()).getUserId());
        }
        if (metaObject.hasSetter("modified")) {
            this.strictUpdateFill(metaObject, "modified", Date.class, new Date());
        }
        if (metaObject.hasSetter("modifier")) {
            //this.strictInsertFill(metaObject, "updatorId", String.class, userOpt.orElse(new UserInfoInSession()).getUserId());
            this.fillStrategy(metaObject, "modifier", userOpt.orElse(new UserInfoInSession()).getUserId());
        }
        /*if (metaObject.hasSetter("creatorName")) {
            this.fillStrategy(metaObject, "creatorName", userOpt.orElse(new UserInfoInSession()).getUserName());
        }*/
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if(MetaContextHelper.isSkipModify()!=null
        && MetaContextHelper.isSkipModify()
        ) return;
        log.info("update fill"+getClassName(metaObject));
        if (metaObject.hasSetter("modified")) {
            Object modified = this.getFieldValByName("modified", metaObject);
            setFieldValByName("modified", new Date(), metaObject);

            //if(ObjectHelper.isEmpty(modified)){
            //    this.strictUpdateFill(metaObject, "modified", Date.class, new Date());
           // }

        }
        Optional<UserInfoInSession> userOpt = Optional.ofNullable(UserSessionLocal.getUser());
        if (metaObject.hasSetter("modifier")) {
            //this.strictInsertFill(metaObject, "updatorId", String.class, userOpt.orElse(new UserInfoInSession()).getUserId());
            //this.fillStrategy(metaObject, "modifier", userOpt.orElse(new UserInfoInSession()).getUserId());
            if(userOpt.isPresent()){
                setFieldValByName( "modifier", userOpt.orElse(new UserInfoInSession()).getUserId(),metaObject);
            }
        }
      /*  if (metaObject.hasSetter("updatorName")) {
            this.fillStrategy(metaObject, "updatorName", userOpt.orElse(new UserInfoInSession()).getUserName());

        }*/
    }
}
