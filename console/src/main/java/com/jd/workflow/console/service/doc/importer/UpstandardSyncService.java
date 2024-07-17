package com.jd.workflow.console.service.doc.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.base.StatusResult;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectOwner;
import com.jd.workflow.metrics.client.RequestClient;
import lombok.Data;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将j-api的用户初始一体化平台的菜单
 */
@Service
public class UpstandardSyncService {
    RequestClient requestClient = new RequestClient();
    static String BASE_URL = "http://api.paas.jd.com";
    @Autowired
    JapiDataSyncService japiDataSyncService;
    @Autowired
    JapiHttpDataImporter japiHttpDataImporter;
    public void updateUserTenantId(Long sceneId,String tenantId){
        StatusResult<List<String>> result = requestClient.get(BASE_URL + "/scene/getAllUser", null, new TypeReference<StatusResult<List<String>>>() {
        });
        if(result.getStatus() != 200){
            throw new RuntimeException("获取用户失败");
        }
        final List<JApiProjectOwner> userList = japiHttpDataImporter.getUserList();
        final Set<String> japiUsers = userList.stream().map(item -> item.getUserName()).collect(Collectors.toSet());
        japiUsers.removeAll(result.getData());
        for (String japiUser : japiUsers) {
            updateScene(sceneId,tenantId,japiUser);
        }
    }

    public void updateScene(Long sceneId,String tenantId,String erp){
        //http://api.paas.jd.com/api/scene/saveUserScenceTest?scenceId=11111erp=''&tenantId=27190
        Map<String,Object> params = new HashedMap();
        params.put("erp",erp);
        params.put("tenantId",tenantId);
        params.put("scenceId",sceneId);
        final StatusResult statusResult = requestClient.get(BASE_URL + "/api/scene/saveUserScenceTest", params, new TypeReference<StatusResult>() {
        });
        if(statusResult.getStatus() != 200){
            throw new RuntimeException("更新配置失败");
        }
    }
    @Data
    public static class UpstandUser{

    }
}
