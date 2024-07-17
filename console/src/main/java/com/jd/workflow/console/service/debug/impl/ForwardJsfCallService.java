package com.jd.workflow.console.service.debug.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.JarJsfDebugDto;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.service.debug.IJsfJarCallService;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ForwardJsfCallService implements IJsfJarCallService {
    static Map<String,ForwardJsfCallService> forwardServiceMap = new ConcurrentHashMap<>();
    RequestClient requestClient;
    private String ip;
    public static ForwardJsfCallService getInstance(String ip){
        return forwardServiceMap.computeIfAbsent(ip,(key)->{
            ForwardJsfCallService forwardJsfCallService = new ForwardJsfCallService(ip);

            return forwardJsfCallService;
        });

    }
    public ForwardJsfCallService(String ip) {
        this.ip = ip;
        requestClient = new RequestClient("http://"+ip,null);
    }
    private Map<String,Object> getHeaders(){
        Map<String,Object> headers = new HashMap<>();
        headers.put("erp", UserSessionLocal.getUser().getUserId());
        return headers;
    }


    @Override
    public boolean reParseJsfJar(MavenJarLocation location) {
        log.info("forward_reparser_call_to:{},location={}",ip,location);
        CommonResult<Boolean> result = requestClient.post("/forwardJsf/reParseJsfJar", null, getHeaders(), location, new TypeReference<CommonResult<Boolean>>() {
        });
        if(!result.isSuccess()){
            throw new BizException(result.getMessage());
        }
        return result.getData();
    }

    @Override
    public JsfOutputExt jarCallJsf(JarJsfDebugDto dto) {
        log.info("forward_jar_call_to:{},location={}",ip,dto.getLocation());
        CommonResult<JsfOutputExt> result = requestClient.post("/forwardJsf/jsfJarDebug", null, getHeaders(), dto, new TypeReference<CommonResult<JsfOutputExt>>() {
        });
        if(!result.isSuccess()){
            throw new BizException(result.getMessage());
        }
        return result.getData();
    }
}
