package com.jd.workflow.console.service.remote.api;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.service.remote.api.dto.jagile.JagileMember;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;

import java.util.List;

/**
 * @author yangzongbin
 * @data 2022/8/31
 * @desc 行云open api
 */
public interface JagileService {

    /**
     * 查询应用信息
     * @return
     */
    List<JDosAppInfo> getJdosAppInfo(String erp);
    public JDosAppInfo getAppInfo(String appName);

    public JagileMember getAppMember(String appName);
}
