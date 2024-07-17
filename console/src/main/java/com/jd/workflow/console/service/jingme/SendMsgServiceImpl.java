package com.jd.workflow.console.service.jingme;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.jim.cli.Cluster;
import com.jd.workflow.console.dto.jingme.ButtonDTO;
import com.jd.workflow.console.dto.jingme.CustomDTO;
import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;
import com.jd.workflow.console.utils.RestTemplateUtils;
import com.jd.workflow.metrics.client.RequestClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27
 */
@Slf4j
@Service
public class SendMsgServiceImpl implements SendMsgService {

    @Resource
    private RestTemplateUtils restTemplateUtils;

    /**
     * redis 客户端
     */
    @Resource(name = "jimClient")
    private Cluster jimClient;


    @Value("${jingme.env:PRE}")
    private String env;


    private String host = "http://openme.jd.local";
    private String TEAM_ACC_TOKEN_KEY = "japi_TeamAccToken";


    @Override
    public String getTeamAccToken() {

        String appAccessToken = getAppToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-stage", env);


        String url = String.format("%s/open-api/auth/v1/team_access_token", host);
        JSONObject body = new JSONObject();
        body.put("appAccessToken", appAccessToken);
        body.put("openTeamId", "0a9c79553a0c15314da1ff1574f4bdc4");

        log.info("## getTeamAccToken ## url:{},header:{}", url, JSON.toJSONString(headers));
        String rest = restTemplateUtils.postJson(url, body.toJSONString(), headers);
        log.info("## getTeamAccToken ## url:{},header:{},result:{}", url, JSON.toJSONString(headers), rest);
        JSONObject result = JSON.parseObject(rest);
        if (result.containsKey("code") && result.getInteger("code") == 0) {
            String token = result.getJSONObject("data").getString("teamAccessToken");
            Integer expireIn = result.getJSONObject("data").getInteger("expireIn");
            jimClient.set(TEAM_ACC_TOKEN_KEY, token, expireIn, TimeUnit.SECONDS, false);
            return token;
        }


        return null;
    }

    @Override
    public String getAppToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-stage", env);


        String url = String.format("%s/open-api/auth/v1/app_access_token", host);
        JSONObject body = new JSONObject();
        body.put("appSecret", "kzRqvylx36U6XDt8UNFg");
        body.put("appKey", "lqbifbDiuaXGBFYpEFy1");

        log.info("## getAppToken ## url:{},header:{}", url, JSON.toJSONString(headers));
        String rest = restTemplateUtils.postJson(url, body.toJSONString(), headers);
        log.info("## getAppToken ## url:{},header:{},result:{}", url, JSON.toJSONString(headers), rest);
        JSONObject result = JSON.parseObject(rest);
        if (result.containsKey("code") && result.getInteger("code") == 0) {
            String token = result.getJSONObject("data").getString("appAccessToken");
            return token;
        }


        return null;
    }

    @Override
    public String sendUserJueMsg(String erp, TemplateMsgDTO templateMsgDTO) {
        Date now = new Date();
        String uuid = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-stage", env);
        String teamToken = jimClient.get(TEAM_ACC_TOKEN_KEY);
        if (StringUtils.isEmpty(teamToken)) {
            teamToken = getTeamAccToken();

        }
        headers.add("authorization", String.format("Bearer %s", teamToken));

        /**拼入参**/
        JSONObject body = new JSONObject();
        {
            JSONObject params = new JSONObject();
            JSONObject data = new JSONObject();
            body.put("appId", "lqbifbDiuaXGBFYpEFy1");
            body.put("erp", erp);
            body.put("tenantId", "CN.JD.GROUP");
            body.put("requestId", uuid);
            body.put("dateTime", now.getTime());
            body.put("params", params);

            params.put("robotId", "00_5e3b6eb047e1435d");
            params.put("data", data);

            data.put("templateType", 1);
            data.put("reload", false);
            data.put("templateId", "templateSystemMsg");

            JSONObject cardData = new JSONObject();
            data.put("cardData", cardData);
            JSONObject header = new JSONObject();
            JSONObject subHeading = new JSONObject();
            JSONObject content = new JSONObject();
            JSONArray customFields = new JSONArray();
            JSONArray buttons = new JSONArray();

            cardData.put("head", header);
            cardData.put("buttons", buttons);
            cardData.put("subHeading", subHeading);
            cardData.put("customFields", customFields);
            cardData.put("content", content);

            header.put("title", templateMsgDTO.getHead());

            if (CollectionUtils.isNotEmpty(templateMsgDTO.getAtUsers())) {
                subHeading.put("atUsers", templateMsgDTO.getAtUsers());
            }
            if (StringUtils.isNotEmpty(templateMsgDTO.getSubHeading())) {
                JSONObject subHeadingTitle = new JSONObject();
                subHeadingTitle.put("text", templateMsgDTO.getSubHeading());
                subHeading.put("title", subHeadingTitle);
            }

            if (CollectionUtils.isNotEmpty(templateMsgDTO.getCustomFields())) {
                for (CustomDTO customField : templateMsgDTO.getCustomFields()) {
                    JSONObject custom = new JSONObject();
                    JSONObject name = new JSONObject();
                    name.put("text", customField.getName());
                    JSONObject description = new JSONObject();
                    description.put("text", customField.getDescription());
                    custom.put("name", name);
                    custom.put("description", description);
                    customFields.add(custom);
                }
            }

            if (StringUtils.isNotEmpty(templateMsgDTO.getContent())) {
                content.put("text", templateMsgDTO.getContent());
            }
            if (CollectionUtils.isNotEmpty(templateMsgDTO.getButtons())) {
                for (ButtonDTO button : templateMsgDTO.getButtons()) {
                    JSONObject btn = new JSONObject();
                    JSONObject deepLink = new JSONObject();
                    deepLink.put("pc", button.getPcUrl());
                    btn.put("eventId", "");
                    btn.put("name", button.getName());
                    btn.put("type", 0);
                    btn.put("deepLink", deepLink);
                    buttons.add(btn);
                }
            }
        }
        /**拼入参结束**/
        String url = String.format("%s/open-api/suite/v1/timline/sendJUEMsg", host);
        log.info("## sendUserJueMsg ## url:{},header:{},body:{}", url, JSON.toJSONString(headers), JSON.toJSONString(body));
        String rest = restTemplateUtils.postJson(url, body.toJSONString(), headers);
        log.info("## sendUserJueMsg ## url:{},result:{}", url, rest);


        return rest;

    }

    @Override
    public void delTeamToken() {
        jimClient.del(TEAM_ACC_TOKEN_KEY);
    }
}
