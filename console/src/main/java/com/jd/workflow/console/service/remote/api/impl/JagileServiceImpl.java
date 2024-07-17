package com.jd.workflow.console.service.remote.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.jsf.gd.util.StringUtils;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.service.remote.api.JagileService;
import com.jd.workflow.console.service.remote.api.dto.jagile.JagileMember;
import com.jd.workflow.console.service.remote.api.dto.jagile.JagileResponse;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
 * 行云open api:http://help.jcd.jd.com/help/api/api-app-controller.html#%E6%9F%A5%E8%AF%A2%E5%BA%94%E7%94%A8%E4%BF%A1%E6%81%AF
 * @author wangjingfang3
 */
@Service
@Slf4j
public class JagileServiceImpl implements JagileService {

    private static final int DEFAULT_TIMEOUT_MILL = 6 * 10 * 1000;
    private static final String J_DOS_BASE_URL = "http://api.jcd.jd.com";
    private static final String J_DOS_ONLINE_API_URL = J_DOS_BASE_URL+"/api/v2/apps?pageNum=1&pageSize=1000";
    private static final String J_DOS_TOKEN_NAME = "token";
    private static final String J_DOS_ONLINE_TOKEN_VALUE = "a879649f-9ae9-41a0-9af9-e2a7656aefef";
    private static final String J_DOS_ERP = "erp";
    private static final String ERP_TESTER = "ext.daojiaceshi";
    private static final String TENANT = "tenant";
    private static final String TENANT_NAME = "JDD";
    RequestClient requestClient = null;
    public JagileServiceImpl(){
        Map<String, Object> headers = new HashMap<>();
        headers.put(J_DOS_TOKEN_NAME, J_DOS_ONLINE_TOKEN_VALUE);
        headers.put(J_DOS_ERP, ERP_TESTER);
        headers.put(TENANT,TENANT_NAME);
        requestClient = new RequestClient(J_DOS_BASE_URL,headers);
    }
    @Override
    public List<JDosAppInfo> getJdosAppInfo(String erp) {
        Map<String, String> headers = new HashMap<>();
        headers.put(J_DOS_TOKEN_NAME, J_DOS_ONLINE_TOKEN_VALUE);
        headers.put(J_DOS_ERP, ERP_TESTER);
        headers.put(TENANT,TENANT_NAME);
        String url = J_DOS_ONLINE_API_URL;
        if(StringUtils.isNotBlank(erp)){
            url = url + "&userErp="+erp;
        }
        String result = get(url, headers);
        if (StringUtils.isBlank(result)) {
            throw new BizException("Jagile api获取应用信息为空！");
        }
        List<JDosAppInfo> jDosAppInfos = new ArrayList<>();
        try {
            JagileResponse response = JsonUtils.parse(result, JagileResponse.class);
            if(response.getSuccess()){
                jDosAppInfos = response.getData().getList();
            }
        } catch (Exception e) {
            log.error("JDos api获取应用信息对象转换异常", e);
        }
        if (CollectionUtils.isEmpty(jDosAppInfos)) {
            throw new BizException("Jagile api获取应用信息为空！");
        }
        return jDosAppInfos;
    }

    private String get(String url, Map<String, String> headers) {
        if (ObjectHelper.isEmpty(url)) {
            return null;
        }
        HttpURLConnection connection = null;
        try {
            connection =  (HttpURLConnection) new URL(url).openConnection();
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
            log.info(String.format("请求url=[%s]出现异常!", url), e);
            throw new BizException("请求出现异常", e);
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public JDosAppInfo getAppInfo(String appName){
         String result = requestClient.get("/api/v2/apps/" + appName, null);

        CommonResult<JDosAppInfo> ret= JsonUtils.parse(result, new TypeReference<CommonResult<JDosAppInfo>>() {
        });
        if(200==ret.getCode()){
            return ret.getData();
        }
        return null;
    }

    public JagileMember getAppMember(String appName){
        String result = requestClient.get("/api/v2/apps/" + appName+"/members", null);

        CommonResult<JagileMember> ret= JsonUtils.parse(result, new TypeReference<CommonResult<JagileMember>>() {
        });
        if(200==ret.getCode()){
            final JagileMember member = ret.getData();
            if(member.getSystemAdmin() == null && member.getSystemOwner() == null){
                return null;
            }
            return member;
        }
        return null;
    }

    public static void main(String[] args) {
        JagileServiceImpl service = new JagileServiceImpl();

         JagileMember members = service.getAppMember("data-flow");
        System.out.println(JsonUtils.toJSONString(members));
    }
}
