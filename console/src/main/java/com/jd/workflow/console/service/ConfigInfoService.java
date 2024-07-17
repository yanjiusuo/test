package com.jd.workflow.console.service;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 配置信息服务类
 */
@Component
public class ConfigInfoService {
    @Value("${interceptor.loginType}")
    Integer loginType;
    @Value("${tenant.fixTenantId:}")
    private String fixTenantId;

    @Value("${tenant.defaultTenantId:up_jd}")
    private String defaultTenantId;



    @Value("${app.forceUpdateAppInfo:false}")
    private boolean forceUpdateAppInfo;

    public boolean isErpLogin(){
        return  LoginTypeEnum.ERP.getCode().equals(loginType);
    }
    public boolean isDependent() {
        return  LoginTypeEnum.SELF.getCode().equals(loginType);
    }

    public boolean isForceUpdateAppInfo() {
        return forceUpdateAppInfo;
    }

    public String getFixTenantId(){
        return fixTenantId;
    }

    public String getDefaultTenantId() {
        return defaultTenantId;
    }
}
