package com.jd.workflow.console.controller;

import cn.hutool.http.HttpRequest;
import com.jd.security.auth.repak.com.gson.Gson;
import com.jd.workflow.console.base.CommonResult;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yuanshuaiming
 * @Date 2022/12/16 10:14 上午
 * @Version 1.0
 */
public class AuditLogControllerTest extends TestCase {
    private static String url = "http://127.0.0.1:8010";
    private static Gson gson = new Gson();

    public void testGetAuditLogList() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", "1681");
//        params.put("methodId", 1681L);
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        params.put("startTime", "2022-12-09 16:22:00");
        params.put("endTime", "2022-12-13 10:00:00");
        params.put("isSuccess", "true");
        params.put("current", "1");
        params.put("size", "2");
        String result2 = HttpRequest.get(url + "/console-log/getAuditLogList").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetLogDetail() {
        Map<String, Object> params = new HashMap<>();
        params.put("interfaceId", "com.jd.bdpops.api.HelloService");
        params.put("methodName", "echoStr");
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        params.put("dateStr", "2022-12-13 10:01:56.824");
        params.put("clientIp", "11.80.9.122");
        params.put("threadName", "JSF-BZ-22000-10-T-2");
        String result2 = HttpRequest.get(url + "/console-log/getLogDetail").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetClientAppNameList() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", 1681L);
        //params.put("cjgAlias", "localdebug");
        String result2 = HttpRequest.get(url + "/console-log/getClientAppNameList").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetInvokeCountByMinute() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", 1681L);
        params.put("startTime", "2022-12-09 16:22:00");
        params.put("endTime", "2022-12-13 10:00:00");
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        String result2 = HttpRequest.get(url + "/console-log/getInvokeCountByMinute").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetInvokeCountByHour() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", 1681L);
        params.put("startTime", "2022-12-09 16:00:00");
        params.put("endTime", "2022-12-13 10:00:00");
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        String result2 = HttpRequest.get(url + "/console-log/getInvokeCountByHour").form(params)
                .execute().body();

        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetInvokeCountByDay() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", 1681L);
        params.put("startTime", "2022-12-09 00:00:00");
        params.put("endTime", "2022-12-13 00:00:00");
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        String result2 = HttpRequest.get(url + "/console-log/getInvokeCountByDay").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }

    public void testGetInvokeCounts() {
        Map<String, Object> params = new HashMap<>();
        params.put("methodId", 1681L);
        params.put("startTime", "2022-12-09 00:00:00");
        params.put("endTime", "2022-12-13 00:00:00");
        params.put("cjgAppName", "ysmtest-jsm");
        params.put("cjgAlias", "localdebug");
        params.put("timeInterval", "day");
        String result2 = HttpRequest.get(url + "/console-log/getInvokeCounts").form(params)
                .execute().body();
        CommonResult rst = gson.fromJson(result2, CommonResult.class);
        System.out.println(gson.toJson(params));
        System.out.println(gson.toJson(rst));
        Assert.assertEquals(0, rst.getCode().intValue());
    }
}