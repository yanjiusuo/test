package com.jd.workflow.console.listener.cjg.processor;

import com.jd.cjg.client.ConvertBeanCommon;
import com.jd.cjg.client.TableClzz;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.listener.cjg.entity.ProviderInfoEntity;
import com.jd.workflow.console.listener.cjg.entity.ProviderInfoEntity;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
*  藏经阁应用成员变更
* */
@Component
@Slf4j
@TableClzz(tablename = "provider_info",clzz= ProviderInfoEntity.class)
public class CjgAppMemberProcessor extends ConvertBeanCommon<ProviderInfoEntity> {
    @Autowired
    CjgHelper cjgHelper;

    @Autowired
    CjgAppConsumerCache appInfoService;

    public void syncApp(List<ProviderInfoEntity> list){
        Map<Integer,ProviderInfoEntity> map = new HashMap<>();
        for (ProviderInfoEntity providerInfoEntity : list) {
            log.info("appMember.err_sync_provider:entity={}",JsonUtils.toJSONString(providerInfoEntity));
            if(providerInfoEntity.getComponentId() == null){
                continue;
            }
            map.putIfAbsent(providerInfoEntity.getComponentId(),providerInfoEntity);
        }
        log.info("appMember.sync_component:componentIds={}",map.keySet());
        for (Map.Entry<Integer, ProviderInfoEntity> entry : map.entrySet()) {
            ProviderInfoEntity providerInfoEntity = entry.getValue();
            String cjgAppCode = cjgHelper.getCjgAppCodeById(providerInfoEntity.getComponentId());
            if(cjgAppCode == null) {
                log.info("appMember.err_cjg_app_code_is_null:componentId={}",providerInfoEntity.getComponentId());
                continue;
            }
            String updateBy = providerInfoEntity.getModifyBy();
            if(StringUtils.isBlank(updateBy)){
                updateBy = "system";
            }
            UserInfoInSession user = new UserInfoInSession(updateBy,updateBy);
            UserSessionLocal.setUser(user);

            appInfoService.syncCjgAppToLocal(cjgAppCode,false);
        }

    }

    @Override
    public void insertEntity(List<ProviderInfoEntity> before, List<ProviderInfoEntity> after) {
        /*log.info("appMember.insert_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(after));
        syncApp(after);*/

    }

    @Override
    public void deleteEntity(List<ProviderInfoEntity> before, List<ProviderInfoEntity> after) {
        /*log.info("appMember.delete_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(before));
        syncApp(before);*/

    }

    @Override
    public void updateEntity(List<ProviderInfoEntity> before, List<ProviderInfoEntity> after) {
       /* log.info("appMember.update_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(after));
        syncApp(after);*/
    }

    @Override
    public void processAll(List<ProviderInfoEntity> updated,List<ProviderInfoEntity> removed) {
        updated.addAll(removed);
        log.info("appMember.update_entity:size={}", updated.size());
        for (ProviderInfoEntity providerInfoEntity : updated) {
            log.info("appMember.update_entity:data={}", JsonUtils.toJSONString(providerInfoEntity));
        }
        syncApp(updated);
    }
}
