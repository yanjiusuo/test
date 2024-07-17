package com.jd.workflow.console.service.depend.impl;

import com.jd.jsf.gd.error.ClientTimeoutException;
import com.jd.jsf.open.api.InterfaceService;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.jsf.open.api.vo.request.QueryMethodInfoRequest;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.service.depend.InterfaceServiceWrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @description: InterfaceService 包装类
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service
@Slf4j
public class InterfaceServiceWrapImpl implements InterfaceServiceWrap {
    @Autowired
    private InterfaceService interfaceService;

    @Override
    public Result<List<InterfaceInfo>> searchInterface(String interfaceName, int pageSize, int startNo) {
        Result<List<InterfaceInfo>> listResult = null;
        try {
            QueryInterfaceRequest queryInterfaceRequest = JSFArgBuilder.buildQueryInterfaceRequest();
            queryInterfaceRequest.setPageSize(pageSize);
            queryInterfaceRequest.setOffset(startNo);
            queryInterfaceRequest.setInterfaceName(interfaceName);
            listResult = interfaceService.searchInterface(queryInterfaceRequest);
        } catch (ClientTimeoutException e) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException interruptedException) {
                log.error("InterfaceServiceWrapImpl.searchInterface InterruptedException ",e);
            }
            //超时重试
            listResult = searchInterface(interfaceName, pageSize, startNo);
            log.error("InterfaceServiceWrapImpl.searchInterface ClientTimeoutException ",e);
        }
        return listResult;
    }

    @Override
    public String getMethodInfo(String interfaceFullName, String methodName) {
        String methodJsonInfo = "";
        try {
            QueryMethodInfoRequest request = JSFArgBuilder.buildSetRequestInfo(new QueryMethodInfoRequest());
            request.setInterfaceName(interfaceFullName);
            request.setMethodName(methodName);
            Result<String> methodInfo = interfaceService.getMethodInfo(request);
            methodJsonInfo = methodInfo.getData();
        } catch (Exception e) {
            log.error("InterfaceServiceWrapImpl.getMethodInfo Exception ", e);
        }
        return methodJsonInfo;
    }

    @Override
    public List<String> getMethodList(String interfaceFullName) {
        List<String> methods = null;
        try {
            QueryMethodInfoRequest queryMethodInfoRequest = JSFArgBuilder.buildSetRequestInfo(new QueryMethodInfoRequest());
            queryMethodInfoRequest.setInterfaceName(interfaceFullName);
            Result<List<String>> result = interfaceService.getMethodList(queryMethodInfoRequest);
            if (Objects.nonNull(result)) {
                methods = result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return methods;
    }
}
