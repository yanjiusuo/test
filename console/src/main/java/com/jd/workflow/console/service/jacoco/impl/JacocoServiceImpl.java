package com.jd.workflow.console.service.jacoco.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.entity.jacoco.JacocoRequestParam;
import com.jd.workflow.console.entity.jacoco.JacocoResult;
import com.jd.workflow.console.service.jacoco.JacocoService;
import com.jd.workflow.console.utils.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Service
@Slf4j
public class JacocoServiceImpl implements JacocoService {

    @Autowired
    private RestTemplateUtils restTemplateUtils;

    private String url = "http://test-local-debug.jd.com/callLocalService";

    /**
     * jacoco服务是否启动
     *
     * @param ip
     * @return
     */
    public boolean isJacocoEnabled(String ip) {
        Boolean retBol = Boolean.FALSE;
        try {
            String jsonParams = buildIsJacocoEnabledParam(ip);
            String response = restTemplateUtils.postJson(url, jsonParams);
            log.info("isJacocoEnabled-> ip:{} response {}", ip, response);
            CommonResult<Boolean> result = JSONObject.parseObject(response, new TypeReference<CommonResult<Boolean>>() {
            });
            if (result.getCode() == 0 && result.getData()) {
                retBol = true;
            }
        } catch (Exception e) {
            log.error("JacocoServiceImpl.isJacocoEnabled Exception ", e);
        }
        return retBol;
    }

    @NotNull
    private String buildIsJacocoEnabledParam(String ip) {
        JacocoRequestParam jacocoRequestParam = new JacocoRequestParam();
        jacocoRequestParam.setMethodName("isJacocoEnabled");
        jacocoRequestParam.setIp(ip);
        Map<String, String> paramData = ImmutableMap.of("ip", ip);
        jacocoRequestParam.getArgs().add(paramData);
        String jsonParams = JSON.toJSONString(jacocoRequestParam);
        return jsonParams;
    }

    /**
     * 开始执行代码覆盖率
     *
     * @param ip
     * @param id
     * @return
     */
    public boolean exportJacoco(String ip, Long id) {
        Boolean retBol = Boolean.FALSE;
        try {
            String jsonParams = buildSetExportJacocoParam(ip, id);
            String response = restTemplateUtils.postJson(url, jsonParams);
            log.info("exportJacoco-> ip: {} id : {} response: {}", ip, id, response);
            CommonResult<Boolean> result = JSONObject.parseObject(response, new TypeReference<CommonResult<Boolean>>() {
            });
            if (result.getCode() == 0 && result.getData()){
                retBol = Boolean.TRUE;
            }
        } catch (Exception e) {
            log.error("JacocoServiceImpl.exportJacoco Exception ", e);
        }
        return retBol;
    }

    private String buildSetExportJacocoParam(String ip, Long id) {
        JacocoRequestParam jacocoRequestParam = new JacocoRequestParam();
        jacocoRequestParam.setMethodName("exportJacoco");
        jacocoRequestParam.setIp(ip);
        Map<String, Object> paramData = ImmutableMap.of("ip", ip, "baseBranch", "master", "id", id);
        jacocoRequestParam.getArgs().add(paramData);
        String jsonParamStr = JSON.toJSONString(jacocoRequestParam);
        return jsonParamStr;
    }

    /**
     * 查询执行进度，返回结果
     *
     * @param ip
     * @param id
     * @return
     */
    public CommonResult<JacocoResult>  queryJacocoStage(String ip, Long id) {
        CommonResult<JacocoResult> result = null;
        try {
            String jsonParams = buildQueryJacocoStageParam(ip, id);
            String response = restTemplateUtils.postJson(url, jsonParams);
            log.info("queryJacocoStage-> ip: {} id : {} response: {}", ip, id, response);
            result = JSONObject.parseObject(response, new TypeReference<CommonResult<JacocoResult>>() {
            });
        } catch (Exception e) {
            log.error("JacocoServiceImpl.queryJacocoStage Exception ", e);
        }
        return result;
    }

    private String buildQueryJacocoStageParam(String ip, Long id) {
        JacocoRequestParam jacocoRequestParam = new JacocoRequestParam();
        jacocoRequestParam.setMethodName("queryJacocoStage");
        jacocoRequestParam.setIp(ip);
        Map<String, Object> paramData = ImmutableMap.of("ip", ip, "baseBranch", "master", "id", id);
        jacocoRequestParam.getArgs().add(paramData);
        String jsonParamStr = JSON.toJSONString(jacocoRequestParam);
        return jsonParamStr;
    }


}
