package com.jd.workflow.console.service.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.jsf.gd.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.test.jagile.DemandDetail;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;


public class JagileRemoteCaller {
    RequestClient client;


    @Autowired
    JagileApiConfig config;
    @PostConstruct
    public void init(){
        client = new RequestClient(config.getHost(),null);
    }

    public JagileApiConfig getConfig() {
        return config;
    }

    public void setConfig(JagileApiConfig config) {
        this.config = config;
    }

    /**
     * 根据需求获取明细
     * @param demandId
     * @return
     */
    public CommonResult<DemandDetail> getDemandById(Long demandId){
        String erp = UserSessionLocal.getUser().getUserId();
        Map<String, Object> headers = config.prepareHeader(erp);
        return client.get("/jacp/api/rest/demand/" + demandId, null,headers, new TypeReference<CommonResult<DemandDetail>>() {
        });
    }

    public CommonResult<DemandDetail> getDemandByCode(String code,String erp){
        //String erp = UserSessionLocal.getUser().getUserId();

        if(StringUtils.isBlank(erp)){
            erp = "cjg";
        }
        Map<String, Object> headers = config.prepareHeader(erp);
        return client.get("/jacp/api/rest/demand/code/" + code, null,headers, new TypeReference<CommonResult<DemandDetail>>() {
        });
    }

    public static void main(String[] args) {
        UserSessionLocal userSessionLocal = new UserSessionLocal();
        UserInfoInSession user = UserSessionLocal.getUser();
        user.setUserId("wangjingfang3");
        UserSessionLocal.setUser(user);
        JagileApiConfig config = new JagileApiConfig();
        JagileRemoteCaller caller = new JagileRemoteCaller();
        config.setHost("http://api-gateway.jd.com");
        config.setAppId("cjg");
        config.setToken("36836bb0-6ae0-4b8d-ae0e-89725137d041");
        config.setAcceptanceUrl("http://jagile.jd.com/to/be/confirm");
        caller.setConfig(config);
        caller.init();
        CommonResult<DemandDetail> result = caller.getDemandByCode("R2023013154090","cjg");
        System.out.println(JsonUtils.toJSONString(result));
    }

    public CommonResult<Map<String,Object>> getDemandByCodeT(String code){
        String erp = UserSessionLocal.getUser().getUserId();
        Map<String, Object> headers = config.prepareHeader(erp);
        return client.get("/jacp/api/rest/demand/code/" + code, null,headers, new TypeReference<CommonResult<Map<String,Object>>>() {
        });
    }


}
