package com.jd.workflow.console.service.test;

import com.alibaba.fastjson.JSONObject;
import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.test.DeepTestBaseDto;
import com.jd.workflow.console.entity.test.TestCaseGroup;
import com.jd.workflow.metrics.client.RequestClient;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class TestCaseGroupServiceTest extends BaseTestCase {

    @Autowired
    private TestCaseGroupService testCaseGroupService;

    @Test
    public void testGetMethod() {
        UserInfoInSession user = new UserInfoInSession();
        user.setUserId("wangjingfang3");
        UserSessionLocal.setUser(user);

        String env = "test";
        Long methodId = 384805L;
        TestCaseGroup testCaseGroup = testCaseGroupService.getMethod(env, methodId);
        System.out.println("result:" + JSON.toJSONString(testCaseGroup));
    }

    @Test
    public void testQueryInterfacesChanged() {
        UserInfoInSession user = new UserInfoInSession();
        user.setUserId("wangjingfang3");
        UserSessionLocal.setUser(user);
        String env = "test";
        Long methodId = 384805L;
        DeepTestBaseDto deepTestBaseDto = new DeepTestBaseDto();
        deepTestBaseDto.setStation(env);
        TestCaseGroup testCaseGroup = testCaseGroupService.getMethod(env, methodId);
        if (Objects.nonNull(testCaseGroup)) {
            deepTestBaseDto.setModuleId(testCaseGroup.getRelatedTestModuleId());
            deepTestBaseDto.setRootSuiteId(testCaseGroup.getRelatedTestCaseGroupId());
        }
        Object ref = testCaseGroupService.queryInterfacesChanged(deepTestBaseDto);
        log.info("result:{}", JSON.toJSONString(ref));

    }

    @Test
    public void testSso() {
        String url = "https://test-ssa.jd.com/sso/login";

//        String body = "fp=SHRSDVJ4XKSVDJMZFV4BBMW3HTESCERHOK57CKI6BYVCABBOIBPTF3EILNQL5AYXXYQZ7PQHAYVBHDUQJZA3DCIGDU&username=wangjingfang3&password=iDbH1l7iQjwMu8OdquhrewQ0O1meK3MLXp%2BnZdydqQA3CmHc%2BGuqWfe3qxBqT1TMAcqR%2FsLWEh1MsUyx2WF1RfpKng8nQRPggqY17E1DhZjYNgiwC40ONp%2FsydJ45hSKR2OyddWwMUzXnTzGmIeZlyRoqERbQTLVGGpyVSNHGG4%3D&uuid=df3dbe7dbb9843558cf3aacf92f64bcb&loginType=1&fidoInfo=&hioXAesKey=&hioResponseBody=&checkCode%3Fif_exists=";
        String body = "username=wangjingfang3&password=QEAYE%2FkuDukv3YDj8MmlXMxAmKo%2Bt839qmYPpxeVmrzRUDTp5PKb19BBcJSEfQz%2BXWy0b05dgphsGb0x0M5Ns1mnAmm9VZNzSPIh8aJ%2FNgosXcPuLfXIAbORU%2B9B5X5IPRISZwfKe45eCzj2m2TRJFXeOBwVcyRY%2BUqp5c70d84%3D&uuid=3dde5528c1c740a28915afa1aa393d09&loginType=1&fidoInfo=&hioXAesKey=&hioResponseBody=&checkCode%3Fif_exists=";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        post(url, body, headers);

    }

    protected ResponseEntity<String> post(String callUrl, String postBody, Map<String, String> headerMap) {


        String ableUrl = callUrl;


        ResponseEntity responseEntity = null;
        String result = "";
        String line = "";
        BufferedReader reader = null;
        String urlPath = ableUrl;
        HttpURLConnection conn = null;
        int responseCode = 0;
        try {
            URL url = new URL(urlPath);
            if (url.openConnection() instanceof HttpURLConnection) {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);                                 // 以后就可以使用conn.getOutputStream().write()
            conn.setDoInput(true);                                  // 以后就可以使用conn.getInputStream().read();
            conn.setUseCaches(false);                               // Post 请求不能使用缓存
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("accept", "application/json");

            conn.setConnectTimeout(60 * 1000 * 10);
            conn.setReadTimeout(60 * 1000 * 10);

            if (!CollectionUtils.isEmpty(headerMap)) {
                for (Map.Entry<String, String> row : headerMap.entrySet()) {
                    conn.setRequestProperty(row.getKey(), row.getValue());
                }
            }


            byte[] writebytes = postBody.getBytes();
            conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
            OutputStream outwritestream = conn.getOutputStream();
            outwritestream.write(postBody.getBytes());
            outwritestream.flush();
            outwritestream.close();


            responseCode = conn.getResponseCode();
            String firstCookie = "";

            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    result += line;
                }

            } else if (responseCode == 302) {
                //获取cookie
                Map<String, List<String>> map = conn.getHeaderFields();
                if (map.containsKey("Set-Cookie")) {
                    List<String> list = map.get("Set-Cookie");
                    StringBuilder builder = new StringBuilder();
                    for (String str : list) {
                        builder.append(str).toString();
                    }
                    firstCookie = builder.toString();
                    System.out.println("第一次得到的cookie=" + firstCookie);
                }


            } else {

                if (conn.getErrorStream() != null) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }

                } else {
                    result = "errorStream 为 null";
                }
            }

        } catch (Exception e) {

            responseCode = 500;
            result = e.getMessage();

        } finally {

            responseEntity = new ResponseEntity(result, HttpStatus.resolve(responseCode));
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }


        return responseEntity;
    }
}