package com.jd.workflow.local;

import com.jd.jsf.gd.util.JSFContext;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.local.entity.CommonResult;
import com.jd.workflow.local.entity.HttpTestServer;
import com.jd.workflow.local.entity.UserDto;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

public class LocalDebugTests extends BaseTestCase {
    /**
     * 测试本地联调功能
     * @throws InterruptedException
     */
    @Test
    public void testDebugLocalJsfInterface() throws InterruptedException {
//        JsfPublisher.main(new String[0]);
        CommonResult<UserDto> result = JsfCaller.call();
        assertEquals("{\"code\":0,\"message\":\"成功\",\"data\":{\"id\":1,\"dept\":null,\"loginType\":null,\"userCode\":\"wjf\",\"userName\":\"测试\",\"password\":\"abc111\"},\"traceId\":null}", JsonUtils.toJSONString(result));
        System.out.println(result);
    }
    @Test
    public void testLocalDebugHttp() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpTestServer.run(6010);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(1000);
        String host = JSFContext.getLocalHost();
         String url = "http://test-local-debug.jd.com/"+host+":6010";
        RequestClient client = new RequestClient(url,new HashMap<>());
        String response = client.get("/test",null);
        assertEquals("This is the response",response);
    }
}
