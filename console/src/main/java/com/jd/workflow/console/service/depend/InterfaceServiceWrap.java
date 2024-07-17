package com.jd.workflow.console.service.depend;

import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;

import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
public interface InterfaceServiceWrap {

    Result<List<InterfaceInfo>> searchInterface(String interfaceName, int pageSize , int startNo);

    /**
     * 获取方法相关信息
     * @param interfaceFullName
     * @param methodName
     */
    String getMethodInfo(String interfaceFullName,String methodName);

    /**
     * 获取接口方法列表
     * @param interfaceFullName
     */
    List<String> getMethodList(String interfaceFullName);
}
