package com.jd.workflow.metrics.client;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.net.URI;
import java.util.Map;

@Slf4j
public class PrometheusReqClient {

    static final String BASE_URL = "http://localhost:9090";
    CloseableHttpClient client;
    public PrometheusReqClient(){


         client = HttpClientBuilder.create().build();
    }
    public String execute(String path,Map<String,Object> params){
        HttpGet req = new HttpGet();

        try {
            String queryStr = StringHelper.encodeQuery(params,"utf-8");
            String uri = BASE_URL+path +"?"+queryStr;
            req.setURI(new URI(BASE_URL+uri));


            CloseableHttpResponse response = client.execute(req);
            if(response.getStatusLine().getStatusCode() != 200){
                log.error("titan.err_query_range:code={},msg={}",response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity(),"utf-8"));
                return null;
            }
            String result = EntityUtils.toString(response.getEntity(),"utf-8");
            return result;
        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }

}
