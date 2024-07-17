package com.jd.workflow.console.service.depend;

import com.jd.jsf.open.api.domain.Server;

import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/28
 */
public interface ProviderServiceWrap {
    /**
     * 查找接口provicer 信息 最多返回10个
     * @param interfaceFullName
     * @return
     */
    List<Server> query(String interfaceFullName);

    /**
     * 获取接口所属 jdos应用code
     * @param interfaceFullName
     * @return
     */
    String getProviderJdosAppCode(String interfaceFullName);
}
