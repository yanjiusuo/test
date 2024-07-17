package com.jd.workflow.console.service.color;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.common.security.MD5;
import com.jd.workflow.console.base.MD5Util;
import com.jd.workflow.console.entity.ColorGatewayParam;
import com.jd.workflow.console.service.IColorGatewayServiceImpl;
import com.jd.workflow.server.dto.color.ColorApiParamDto;
import com.jd.workflow.soap.common.method.ColorGatewayParamDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ColorApiServiceImpl {

    @Resource
    private IColorGatewayServiceImpl colorGatewayService;

    @Value("${color.cluster:http://sstp-color-betaloginintercept.sstp-oceanapi.svc.ht1.n.jd.local/open-api/v2/}")
    private String colorHost;

    @Value("${color.token:192ff9a14a0f4b179356150abd9559ee}")
    private String colorToken;

    /**
     *
     * http://xbp.jd.com/ticket/mine/10040613
     */
    private String secKey="1/KWoFuEkSceBKoV4HOpOw==";

    @Value("${color.ip:127.0.0.1}")
    private String ip;

    /**
     *
     * @param functionId
     * @param userPin
     * @param zoneCluster api.m.jd.com
     * @return
     */

    /**
     * 根据fuctionId查询color信息 todo color 提供
     *
     * 对接人：yunxiao1
     * @param functionId
     * @param userPin
     * @param zoneCluster
     * @return
     */
    public List<ColorApiParam> queryColorInfoByFunctionId(String functionId, String userPin, String zoneCluster) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String host =colorHost+"api/customParam/search?";
            HttpGet get = new HttpGet();
            addHeaderInfo(get);
            get.setURI(new URI(String.format(host + "functionId=%s&clusterDomain=%s", functionId, zoneCluster)));
            log.info("queryColorInfoByFunctionId:{},{}",get.getURI().toString(),JSONObject.toJSONString(get.getAllHeaders()));
            CloseableHttpResponse result = httpClient.execute(get);
            if (result.getEntity() != null) {
                String params = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
                log.info("result:{}",JSONObject.toJSONString(params));
                List<ColorApiParam> apiParams = JSONArray.parseArray(JSONObject.parseObject(params).getString("data"), ColorApiParam.class);
                return apiParams;
            }
        } catch (Exception e) {
            log.error("查询color接口异常", e);
        }
        return null;
    }
    /**
     *
     * @param functionId
     * @param env prod或者beta
     * @return
     */
    public List<ColorCluster> queryApiClusterListByFunctionId(String functionId,String env) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String host = colorHost+"apiCluster/search?";
            HttpGet get = new HttpGet();
            addHeaderInfo(get);
            get.setURI(new URI(String.format(host + "functionId=%s&env=%s",functionId,env)));
            log.info("queryApiClusterListByFunctionId:{},{}",get.getURI().toString(),JSONObject.toJSONString(get.getAllHeaders()));
            CloseableHttpResponse result = httpClient.execute(get);
            if (result.getEntity() != null) {
                String params = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
                log.info("result:{}",JSONObject.toJSONString(params));
                List<ColorCluster> apiParams = JSONArray.parseArray(JSONObject.parseObject(params).getString("data"), ColorCluster.class);
                //跟color沟通，只展示常用的zone
                return  apiParams.stream().filter(i->i.getName().equals("api.m.jd.com")||i.getName().equals("api.m.jd.care")).collect(Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            log.error("查询zone接口异常", e);
            throw e;
        }
    }


    public List<ColorApiSimple> fuzzySearch(String functionId) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String host = colorHost+"api/apiBaseInfo/fuzzySearch?";
            HttpGet get = new HttpGet();
            addHeaderInfo(get);
            get.setURI(new URI(String.format(host + "functionId=%s",functionId)));
            log.info("fuzzySearch:{},{}",get.getURI().toString(),JSONObject.toJSONString(get.getAllHeaders()));
            CloseableHttpResponse result = httpClient.execute(get);
            if (result.getEntity() != null) {
                String params = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
                log.info("result:{}",JSONObject.toJSONString(params));
                List<ColorApiSimple> apiParams = JSONArray.parseArray(JSONObject.parseObject(params).getString("data"), ColorApiSimple.class);
                return apiParams;
            }
            return null;
        } catch (Exception e) {
            log.error("查询zone接口异常", e);
            throw e;
        }
    }

    /**
     * 申请 http://xbp.jd.com/22/apply/19853
     * @return
     */
    public void addHeaderInfo(HttpGet get){
        String timestamp = System.currentTimeMillis() + "";
        String appCode = "data-flow";
        get.setHeader("secKey", secKey);
        get.setHeader("appCode", appCode);
        get.setHeader("timestamp", timestamp);
        get.setHeader("ip", ip);
        log.info("header{},md5结果{}",appCode+timestamp+secKey+ip,new MD5().getMD5ofStr(appCode+timestamp+secKey+ip));
        get.setHeader("sign", new MD5().getMD5ofStr(appCode+timestamp+secKey+ip));
    }

}
