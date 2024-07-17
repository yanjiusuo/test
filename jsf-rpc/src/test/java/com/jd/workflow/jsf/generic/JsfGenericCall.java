package com.jd.workflow.jsf.generic;

import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：JsfGenericCall
 * 类 描 述：TODO
 * 创建时间：2022-06-28 17:17
 * 创 建 人：wangxiaofei8
 */
public class  JsfGenericCall {

    public static void main(String[] args) {
        JsfApi jsfApi = new JsfApi();
        jsfApi.setAlias("CHANGE-IT");
        jsfApi.setJsfInterface("com.jd.testjsf.HelloServiceAllen");
        jsfApi.setProtocol("jsf");
        jsfApi.setSerialization("hessian");
        jsfApi.setTimeout(3000);
        jsfApi.setIndex("i.jsf.jd.com");
        jsfApi.setGeneric(true);
        //设置头信息
        List<JsfParameterConfig> header = new ArrayList<>();
        JsfParameterConfig parameterConfig = new JsfParameterConfig();
        parameterConfig.setHide(true);
        parameterConfig.setKey("testheaderKey");
        parameterConfig.setValue("testheaderValue");
        header.add(parameterConfig);
        jsfApi.setConfigParams(JsonUtils.toJSONString(header));

        JsfClientProxy proxy = JsfClientFactory.getProxy(jsfApi);

        while(true){
            String method = "echoHello";
            String parameterTypes="java.lang.Long,com.jd.testjsf.domain.User";
            String parameterNames="uid,user";
            String srcValue = "{\"uid\":63568,\"user\":{\"id\":3,\"name\":\"wangxf\",\"list\":[\"jiahao\",\"ziqing\",\"lili\"]}}";
            Map<String,Object> paramValueMap = JsonUtils.parse(srcValue, Map.class);
            GenericRequest request = GenericRequest.buildRequest(method,parameterTypes,parameterNames,paramValueMap);
            Object response = proxy.invoke(request);
            System.out.println(JsonUtils.toJSONString(response));
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
    }
}
