package com.jd.workflow.console.service.impl;


import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.base.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 物料平台api封装service
 */
@Service
@Slf4j
public class OpenApiService {


    private String url = "http://test-data-server.jd.com/";

    private static final String excuteUrl="openapi/v1/tool/execute";

    private static final String token="11725d703c45093a1d33931df2f54103c85632440fd87601";

    /**
     * 物料平台 获取接口信息
     * @param body
     * @return
     */
    public String testDataExecute(JSONObject body) {

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json;charset=UTF-8");
        headers.put("appid", "japi");
        String timestamp = System.currentTimeMillis() + "";
        String session = MD5Util.MD5Encode(token + timestamp);
        headers.put("timestamp", timestamp);
        headers.put("session", session);
        try {
            return HttpRequest.post(url + excuteUrl).headerMap(headers, false)
                    .body(JSONObject.toJSONString(body)).execute().body();

        } catch (HttpException e) {
            log.error("物料平台接口调用异常",e);
        }
        return null;

    }
}
