package com.jd.workflow.console.service.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.mock.HttpDemoValue;
import com.jd.workflow.console.dto.mock.JsfDemoValue;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.y.response.ReplyVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
/*
@王井方  提供给前端的查看mockjs返回值的接口，看看这样可行么？
接口地址：/api/convert/convertText

入参对象：ConvertTextQO
{
	"sourceType": 1,  //1 - HTTP， 2 - JSF
	"content": ""
}

返回值对象：
ReplyVO<ConvertTextVO>，成功时ConvertTextVO对象非空，失败时ConvertTextVO对象为空

ConvertTextVO对应：
{
	"sourceType": 1,  //1 - HTTP， 2 - JSF
	"content": ""
}
 */
@Service
@Slf4j
public class EasyMockHttpService {
    public static final String MOCK_URL = "http://mock-dev.jd.com";
    public static final String GET_MOCK_DATA = "/test/open/api/convert/convertText";
    @Value("${mock.url:http://mock-dev.jd.com}")
    private String mockUrl = "http://mock-dev.jd.com";
    @Value("${mock.getMockDataUrl:/test/open/api/convert/convertText}")
    private String getMockDataUrl = "/test/open/api/convert/convertText";
     RequestClient requestClient;
     @PostConstruct
    public void init(){
        requestClient = new RequestClient();
    }

    public  String getMockContent(String json, InterfaceTypeEnum interfaceTypeEnum){
        Map<Integer,Integer> typeMap = new HashMap<>();
        typeMap.put(InterfaceTypeEnum.HTTP.getCode(),1);
        typeMap.put(InterfaceTypeEnum.JSF.getCode(),2);
        typeMap.put(InterfaceTypeEnum.EXTENSION_POINT.getCode(),1);

        Map<String,Object> headers = new HashMap<>();
        headers.put("token","ipaas");

        Map<String,Object> params = new HashMap<>();
        params.put("platform",1);

        ContentTypeVo  body = new ContentTypeVo();
        body.setSourceType(typeMap.get(interfaceTypeEnum.getCode()));
        body.setContent(json);
        log.info("easymock.request_mock_request:url={},params={},body={}",mockUrl + getMockDataUrl,params,body);
        try{
            ReplyVO<ContentTypeVo> result  = requestClient.post(RequestClient.RequestBuilder.create().path(mockUrl + getMockDataUrl).params(params).headers(headers).body(body), new TypeReference<ReplyVO<ContentTypeVo>>() {
            });
            if(result != null && result.getData() != null){
                return result.getData().getContent();
            }
        }catch (Exception e){
            return null;
        }


        return null;
    }
    public  HttpDemoValue buildHttpMockValue(HttpDemoValue httpDemoValue){
        String result = getMockContent(JsonUtils.toJSONString(httpDemoValue), InterfaceTypeEnum.HTTP);
        if(result == null) return null;
        return JsonUtils.parse(result,HttpDemoValue.class);
    }

    public  JsfDemoValue buildJsfMockValue(JsfDemoValue httpDemoValue){
        String result = getMockContent(JsonUtils.toJSONString(httpDemoValue), InterfaceTypeEnum.JSF);
        if(result == null) return null;
        return JsonUtils.parse(result,JsfDemoValue.class);
    }
    @Data
    public static class ContentTypeVo{
        Integer sourceType;
        String content;
    }
}
