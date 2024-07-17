package com.jd.workflow.console.service.manage;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.service.IColorGatewayServiceImpl;
import com.jd.workflow.console.service.color.ColorApiParam;
import com.jd.workflow.console.service.color.ColorCluster;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GraphListServiceImpl {


    @Value("${cjg.cluster:http://cjg-api.jd.com/api/}")
    private String colorHost;


    /**
     *
     * @param cjgProductTrace
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<GraphApp> queryAppList(String cjgProductTrace,String domainTrace,Long pageNo,Long pageSize) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String host = colorHost+"unapp/graphList?";
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(String.format(host)));
            httpPost.addHeader("content-Type", "application/json;charset=utf-8;");
            Map<String,Object> body=new HashMap<String,Object>();
            body.put("productTrace",cjgProductTrace);
            body.put("domainTrace",domainTrace);
            body.put("pageNo",pageNo);
            body.put("pageSize",pageSize);
            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(body), StandardCharsets.UTF_8));
            CloseableHttpResponse result = httpClient.execute(httpPost);
            if (result.getEntity() != null) {
                String params = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
                List<GraphApp> apiParams = JSONArray.parseArray(JSONObject.parseObject(params).getJSONObject("data").getString("records"), GraphApp.class);
                return apiParams;
            }
        } catch (Exception e) {
            log.error("查询color接口异常", e);
        }
        return null;
    }
}
