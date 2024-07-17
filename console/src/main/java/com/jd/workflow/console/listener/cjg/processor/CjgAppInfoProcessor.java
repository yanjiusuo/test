package com.jd.workflow.console.listener.cjg.processor;

import com.jd.cjg.client.ConvertBeanCommon;
import com.jd.cjg.client.TableClzz;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.listener.cjg.entity.ComponentInfoEntity;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@TableClzz(tablename = "component_info",clzz= ComponentInfoEntity.class)
public class CjgAppInfoProcessor extends ConvertBeanCommon<ComponentInfoEntity> {
    @Autowired
    CjgAppConsumerCache appInfoService;
    private void syncCjgApp(List<ComponentInfoEntity> cjgApps,boolean delete){
        Set<String> appNames = cjgApps.stream().filter(vs->{
            return vs.getCompontentTypeId() != null && vs.getCompontentTypeId() == 1;
        }).map(vs->vs.getName()).collect(Collectors.toSet());
        log.info("app.sync_cjg_app:appNames={}",appNames);
        for (String appName : appNames) {
            log.info("app.sync_cjg_app_to_local:appName={},delete={}",appName,delete);
            String updateBy = "system";
            if(StringUtils.isBlank(updateBy)){
                updateBy = "system";
            }
            UserInfoInSession user = new UserInfoInSession(updateBy,updateBy);
            UserSessionLocal.setUser(user);
            appInfoService.syncCjgAppToLocal(appName,delete);
        }

    }
    @Override
    public void insertEntity(List<ComponentInfoEntity> before, List<ComponentInfoEntity> after) {
       /* log.info("app.insert_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(after));
        syncCjgApp(after,false);*/

    }

    @Override
    public void deleteEntity(List<ComponentInfoEntity> before, List<ComponentInfoEntity> after) {

       /* log.info("app.delete_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(after));
        syncCjgApp(before,true);*/
    }

    @Override
    public void updateEntity(List<ComponentInfoEntity> before, List<ComponentInfoEntity> after) {

        /*log.info("app.update_entity:before={},after={}", JsonUtils.toJSONString(before), JsonUtils.toJSONString(after));
        syncCjgApp(after,false);*/
    }

    @Override
    public void processAll(List<ComponentInfoEntity> updated, List<ComponentInfoEntity> removed) {
        log.info("app.processAppData:data={}", updated.size()+removed.size());
        for (ComponentInfoEntity componentInfoEntity : updated) {
            log.info("app.updated_entity:before={}", JsonUtils.toJSONString(componentInfoEntity));
        }
        for (ComponentInfoEntity componentInfoEntity : removed) {
            log.info("app.removed_entity:before={}", JsonUtils.toJSONString(componentInfoEntity));
        }
        syncCjgApp(updated,false);
        syncCjgApp(removed,true);
    }


}
