package com.jd.workflow.ducc;

import com.jd.workflow.metrics.client.RequestClient;

import java.util.HashMap;
import java.util.Map;

public class DuccOpenApiTest {
    static String baseUri ="http://test.ducc-api.jd.local";
    static RequestClient requestClient;
    static {
        Map<String,Object> headers = new HashMap<>();
        headers.put("","");
        requestClient = new RequestClient(baseUri,headers);
    }
    public String getDuccConfig(){
        String url = "/v1/namespace/:nsId/configs";
        return null;
    }
}
