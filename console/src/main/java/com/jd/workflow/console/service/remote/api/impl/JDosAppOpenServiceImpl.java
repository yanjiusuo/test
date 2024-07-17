package com.jd.workflow.console.service.remote.api.impl;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.service.remote.api.JDosAppOpenService;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.service.remote.api.dto.jdos.JdosAppMembers;
import com.jd.workflow.console.service.remote.api.dto.jdos.SystemAppInfo;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coding代码库开放服务
 *
 * @author: jialixan
 * @date: 2021/11/02
 */
@Service
public class JDosAppOpenServiceImpl implements JDosAppOpenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDosAppOpenServiceImpl.class);
    private static final int DEFAULT_TIMEOUT_MILL = 5000;
    //JDos api
    private static final String J_DOS_ONLINE_API_URL = "http://api.jdos.jd.com/api/system/normal";
    private static final String J_DOS_TOKEN_NAME = "token";
    private static final String J_DOS_ONLINE_TOKEN_VALUE = "a879649f-9ae9-41a0-9af9-e2a7656aefef";
    private static final String J_DOS_ERP = "erp";
    public static final String ERP_TESTER = "ext.daojiaceshi";
    private static String GATEWAY_DOMAIN_URL = "http://api.jcd-gateway.jd.com";
    private static String GATEWAY_DOMAIN_TOKEN = "b820c20a-c93d-4660-9d29-a963b67d0ce2";
    private static final String MEMBERS_URI = "/api/v2/apps/%s/members";
    private static final String APPINO_URI = "/api/v2/apps/%s";
    //罗汉堂token
    private static String TOKEN_LHT_ERP = "org.lht";

    @Override
    public List<SystemAppInfo> findSysAppInfo() {
        Map<String, String> headers = new HashMap<>();
        headers.put(J_DOS_TOKEN_NAME, J_DOS_ONLINE_TOKEN_VALUE);
        headers.put(J_DOS_ERP, ERP_TESTER);
        String result = get(J_DOS_ONLINE_API_URL, headers);
        if ("[]".equals(result) || null == result) {
            throw new BizException("JDos api获取应用信息为空！");
        }
        List<SystemAppInfo> systemAppInfoList = new ArrayList<>();
        /*try {
            JsonNode jsonNode = JsonHelper.getMapper().readTree(result);
            if (!"Success".equals(JsonHelper.toJson(jsonNode.get("code")).replaceAll("\\\"", ""))) {
                throw new BizException("JDos api获取应用信息失败！");
            }
            systemAppInfoList = JsonHelper.fromJsonArray(jsonNode.get("data").toString(), SystemAppInfo.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("JDos api获取应用信息对象转换异常", e);
        }*/
        if (ObjectHelper.isEmpty(systemAppInfoList)) {
            throw new BizException("JDos api获取应用信息为空！");
        }
        return systemAppInfoList;
    }
    //消息头

    @Override
    public JdosAppMembers queryJdosAppMembersAppCode(String appCode, String site) {
        Map<String, String> headers = new HashMap<>();
        headers.put(J_DOS_TOKEN_NAME, J_DOS_ONLINE_TOKEN_VALUE);
        headers.put(J_DOS_ERP, ERP_TESTER);
        String url = GATEWAY_DOMAIN_URL + String.format(MEMBERS_URI, appCode);
        String response = get(url,headers);
        CommonResult<JdosAppMembers> result = JsonUtils.parse(response, new TypeReference<CommonResult<JdosAppMembers>>() {
        });
        //分页差不多数据
        return result.getData();
    }

    @Override
    public JDosAppInfo queryJdosAppInfo(String appCode, String site) {
        Map<String, String> headers = new HashMap<>();
        headers.put(J_DOS_TOKEN_NAME, J_DOS_ONLINE_TOKEN_VALUE);
        headers.put(J_DOS_ERP, ERP_TESTER);
        String url = GATEWAY_DOMAIN_URL + String.format(APPINO_URI, appCode);
        String response = get(url,headers);
        CommonResult<JDosAppInfo> result = JsonUtils.parse(response, new TypeReference<CommonResult<JDosAppInfo>>() {
        });
        //分页差不多数据
        return result.getData();
    }
    private String get(String url, Map<String, String> headers) {
        if (ObjectHelper.isEmpty(url)) {
            return null;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MILL);
            connection.setReadTimeout(DEFAULT_TIMEOUT_MILL);
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            if (!ObjectHelper.isEmpty(headers)) {
                headers.forEach(connection::setRequestProperty);
            }
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("http error,code=" + connection.getResponseCode());
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()))) {
                String line;
                StringBuilder responseBuilder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                return responseBuilder.toString();
            }
        } catch (Exception e) {
            LOGGER.info(String.format("请求url=[%s]出现异常!", url), e);
            throw new BizException("请求出现异常", e);
        }
    }

    public static void main(String[] args) {
        JDosAppOpenServiceImpl jDosAppOpenService = new JDosAppOpenServiceImpl();
        JDosAppInfo result = jDosAppOpenService.queryJdosAppInfo("mpaas2-main", "cn");
        System.out.println(JSONObject.toJSONString(result));
    }

}
