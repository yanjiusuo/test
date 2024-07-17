package com.jd.workflow.console.service.doc.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.doc.importer.dto.CjgDomain;
import com.jd.workflow.console.service.doc.importer.dto.CjgListDomain;
import com.jd.workflow.console.service.doc.importer.dto.CjgPageResult;
import com.jd.workflow.console.service.doc.importer.dto.CjgQueryDomainRecord;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Component
public class CjgApiImporter {
    public static final String CJG_API_URL = "http://cjg-api.jd.com";
    static String DOMAIN_PATH = "/api/lb/app/page";

    String DOMAIN_DETAIL_TRACE = "/api/KgExternalInterface/businessDomainList";
    //RequestClient requestClient;

    BasicCookieStore store;



    @PostConstruct
    public void init() {
        store = new BasicCookieStore();

       // requestClient = new RequestClient(store);
    }

    public String getDomainTrace(String domainCode,String cookie){
        RequestClient requestClient = new RequestClient();
        //needPermision=true&keyword=houduanjishuyu.PaaShuakaifangyu&domainType=1&dataSource=2
        Map<String,Object> params = new HashMap<>();
        params.put("needPermision",true);
        params.put("keyword",domainCode);
        params.put("domainType",1);
        params.put("dataSource",2);
        /*BasicClientCookie jdCookie = new BasicClientCookie("sso.jd.com", cookie);
        jdCookie.setDomain(".jd.com");
        jdCookie.setAttribute(ClientCookie.DOMAIN_ATTR,"true");
        jdCookie.setPath("/");
        store.clear();
        store.addCookie(jdCookie);*/
        CjgResult<List<CjgDomain>> result = requestClient.get(CJG_API_URL + DOMAIN_DETAIL_TRACE, params,newCookie(cookie), new TypeReference<CjgResult<List<CjgDomain>>>() {
        });
        log.info("cjg.query_domain_result:domainCode={},result={}",domainCode,result);
        if(!ObjectHelper.isEmpty(result.getData())){
            for (CjgDomain datum : result.getData()) {
                CjgDomain found = datum.findByCode(domainCode);
                if(found != null){
                    return found.getTrace();
                }
            }
        }
        return null;
    }
   static Map<String,Object> newCookie(String cookie){
        Map<String,Object> headers = new HashMap<>();
       headers.put("Cookie",cookie);
        return headers;
    }
    public List<String> queryCjgBusinessDomainList(String appCode,String cookie){
        BasicCookieStore store = new BasicCookieStore();
        RequestClient requestClient = new RequestClient();


        Map<String,Object> body = new HashMap<>();
        body.put("pageIndex",1);
        body.put("pageSize",10);
        body.put("search",appCode);


        CjgResult<CjgPageResult<List<CjgQueryDomainRecord>>> result = requestClient.post("http://cjg-api.jd.com/api/lb/app/page", null,newCookie(cookie),
                body, new TypeReference<CjgResult<CjgPageResult<List<CjgQueryDomainRecord>>>>() {
        });
        log.info("cjg.query_app_result:appCode={},result={}",appCode,result);
        if(result.isSuccess()
        && !ObjectHelper.isEmpty(result.getData())
        ){
            List<CjgQueryDomainRecord> records = result.getData().getRecords();
            if(!ObjectHelper.isEmpty(records)){
                List<CjgListDomain> domains = records.get(0).getDomains();
                if(!ObjectHelper.isEmpty(domains)){
                    return domains.stream().map(item->item.getDomainCode()).collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> queryAppDomains(String cookie,String appCode){
        List<String> result = new ArrayList<>();
        List<String> domainCodes = queryCjgBusinessDomainList(appCode, cookie);
        if(domainCodes.isEmpty()){
           return result;
        }
        for (String domainCode : domainCodes) {
            String domainTrace = getDomainTrace(domainCode, cookie);
            if(domainTrace != null){
                result.add(domainTrace);
            }
        }
        return result;
    }
    @Data
    public static class CjgResult<T>{
        Integer status;
        String message;
         T data;
        public boolean isSuccess(){
            return status != null && status.equals(200);
        }
    }

    public static void main(String[] args) throws Exception {
        // 创建URL对象和HttpURLConnection对象
        URL url = new URL("http://cjg-api.jd.com/api/lb/app/page");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求方法为POST
        connection.setRequestMethod("POST");

        // 打开输出流，并写入请求体中的参数
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie"," sso.jd.com=BJ.6330F36AF9198E50F30F9E517D6866CD.6920230821101950; ssa.jd-sep-web-shop=59173b6c2e72c23e686dbdbf8889d06b0f46a5efb90c0039572b428b59f29fe135f13e97770fc1e2e7f869f6acf7e5a4262d4a2cd00c59bc4db4266579c808ecc64952c083d8061a31b770d294f05bf8e0d657e4cd91889f4605b0e1dce38aa76504148e5f8b773ef29f5fbb8ac4afb9052134ba5f090ee8d80b341283238d41; ssa.global.ticket=ac76f9d63e33ba9a133c7d06319da6e69180334fcde4c7e4e87393bd233a1866;");
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        String json = "{\"search\":\"data-flow\",\"pageIndex\":1,\"pageSize\":10}";
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

        // 发送请求
        int responseCode = connection.getResponseCode();

        // 读取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // 打印响应内容和状态码
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response Body: " + response.toString());
    }
}
