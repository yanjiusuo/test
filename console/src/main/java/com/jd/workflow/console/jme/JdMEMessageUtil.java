package com.jd.workflow.console.jme;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.workflow.console.entity.InterfaceManage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 发送京ME消息工具类
 * @author xiaobei
 * @date 2022-12-21 19:57
 */
@Slf4j
public class JdMEMessageUtil {

    /**
     * 获取访问凭证
     */
    private static final String GET_ACCESS_TOKEN_URL = "http://open.timline.jd.com/open-apis/v1/auth/get_access_token";

    /**
     * 发送通知消息
     */
    private static final String SEND_MESSAGE_URL = "http://open.timline.jd.com/open-apis/v1/messages/notice";

    /**
     * ~cjgpaas 标识对应的appKey
     */
    private static final String CJG_PASS_APP_KEY = "00_da7f2d5a789c47d9";

    /**
     * ~cjgpaas 标识对应的appSecret
     */
    private static final String CJG_PASS_APP_SECRET = "cd96668e462a4cee9e90582e2f62cb31";

    /**
     * cjg对应的标签
     */
    private static final String CJG_PASS_TAG = "~cjgpaas";

    private static final String accessTokenCacheKey = "access_token";

    /**
     * accessToken 缓存
     */
    private static Cache<String, String> accessTokenCache = CacheBuilder.newBuilder()
            .concurrencyLevel(16)
            .initialCapacity(1)
            .maximumSize(1)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    private void putAccessToken(String token) {
        accessTokenCache.put(accessTokenCacheKey, token);
    }

//    private String getAccessToken() {
//        return accessTokenCache.getIfPresent(accessTokenCacheKey);
//    }

    public static JdMEResult<JdMeAccessToken> getAccessToken() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GET_ACCESS_TOKEN_URL);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.addHeader("X-Request-Id", UUID.randomUUID().toString());
        JdMEAccessTokenRequest tokenRequest = new JdMEAccessTokenRequest();
        tokenRequest.setApp_key(CJG_PASS_APP_KEY);
        tokenRequest.setApp_secret(CJG_PASS_APP_SECRET);
        String body = JSON.toJSONString(tokenRequest);
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            JdMEResult<JdMeAccessToken> repResult
                    = JSON.parseObject(result, new TypeReference<JdMEResult<JdMeAccessToken>>() {});
            JdMeAccessToken jdMeAccessToken;
            if(repResult != null && (jdMeAccessToken = repResult.getData()) != null) {
                // Integer effectiveSecTime = jdMeAccessToken.getEffective_time();
                String accessToken = jdMeAccessToken.getAccess_token();
                String oldAccessToken = accessTokenCache.getIfPresent(accessToken);
                if(!ObjectUtils.nullSafeEquals(accessToken, oldAccessToken)) {
                    // 更新accessToken
                    accessTokenCache.put(accessTokenCacheKey, accessToken);
                }
            }
            log.info("请求结果为：{}", result);
            return repResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JdMEResult.error(-1);

    }

    /**
     * 发送京ME消息
     * @param msg
     * @return
     */
    public static JdMEResult<String> sendMessage(@Valid JdMENoticeMessage msg,
                                                 @NotNull(message = "凭证不能为空") String authorization) {
        return sendMessage(msg, () -> authorization);
    }


    /**
     * 发送京ME消息
     * @param msg
     * @return
     */
    private static JdMEResult<String> sendMessage(@Valid JdMENoticeMessage msg, Supplier<String> authorizationSupplier) {
        if(msg == null) {
            return JdMEResult.error(-1, "发送京东ME消息时请求参数不能为空");
        }
        if(StringUtils.isEmpty(msg.getNotice_id())) {
            msg.setNotice_id(CJG_PASS_TAG);
        }
        if(StringUtils.isEmpty(msg.getApp())) {
            // 默认国内
            msg.setApp("ee");
        }
        if(msg.getTo_terminal() == null) {
            // 全部终端：7
            msg.setTo_terminal(7);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(SEND_MESSAGE_URL);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.addHeader("X-Requested-Id", UUID.randomUUID().toString());
        httpPost.addHeader("Authorization", authorizationSupplier.get());
        String body = JSON.toJSONString(msg);
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            JdMEResult<String> repResult
                    = JSON.parseObject(result, new TypeReference<JdMEResult<String>>() {});
            log.info("请求结果为：{}", result);
            return repResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JdMEResult.error(-1);
    }


    /**
     * 发送京ME消息
     * @param msg
     * @return
     */
    public static JdMEResult<String> sendMessage(@Valid JdMENoticeMessage msg) {
        JdMEResult<String> repResult = sendMessage(msg, () -> {
            String accessToken = accessTokenCache.getIfPresent(accessTokenCacheKey);
            if (StringUtils.isEmpty(accessToken)) {
                getAccessToken();
            }
            return accessTokenCache.getIfPresent(accessTokenCacheKey);
        });
        boolean tokenError = repResult != null
                && !repResult.getSuccess()
                && (JdMECodeEnum.TOKEN_HAS_EXPIRED_PLEASE_APPLY_AGAIN.getCode().equals(repResult.getCode())
                || JdMECodeEnum.ACCESS_CREDENTIALS_ARE_EMPTY.getCode().equals(repResult.getCode()));
        if(tokenError) {
            // 若token过期，此处需要重新获取
            getAccessToken();
            return sendMessage(msg, () -> accessTokenCache.getIfPresent(accessTokenCacheKey));
        }
        return repResult;
    }
}
