package com.jd.workflow.console.service;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.ConvertWebServiceBaseDto;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.dto.WebServiceInputDTO;
import com.jd.workflow.console.dto.WebServiceOutputDTO;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Test {


    public static void main(String[] args) {
        Test test = new Test();
        //test.testAddHttpToWebService();
        test.testVersion();

    }

    public void testVersion(){
        InterfaceInfoReq req = new InterfaceInfoReq();
        req.setInterfaceId(1l);
        req.setVersion("1.0.3");
        System.out.println(JsonUtils.toJSONString(req));
    }

    public void testAddHttpToWebService(){

        HttpToWebServiceDTO dto = new HttpToWebServiceDTO();
        //dto.setId(1l);
        dto.setInterfaceId(1l);
        dto.setMethodId(1l);
        dto.setPkgName("com.jd.console");

        dto.setMethodName("findSkuInfo");

        dto.setEnv("测试环境");


        WebServiceInputDTO input = new WebServiceInputDTO();
        input.setReqType("json");

        List<JsonType> headers = new ArrayList<>();
        JsonType o1 = new SimpleJsonType(SimpleParamType.STRING,"Content-Type");
        o1.setValue("application/json");
        o1.setRequired(true);
        o1.setDesc("请求头信息");
        headers.add(o1);
        input.setHeaders(headers);


        List<JsonType> body = new ArrayList<>();

        JsonType o2 = new SimpleJsonType(SimpleParamType.LONG,"skuId");
        o2.setValue(66666l);
        o2.setRequired(true);
        o2.setDesc("商品skuId");
        body.add(o2);

        ObjectJsonType o3 = new ObjectJsonType();
        o3.setRequired(true);
        o3.setName("user");
        o3.setDesc("请求用户对象");
        o3.setClassName("com.jd.console.User");

        JsonType o31 = new SimpleJsonType(SimpleParamType.STRING,"userPin");
        o31.setRequired(true);
        o31.setValue("test_admin_pin");
        o31.setDesc("请求用户1pin");
        o3.getChildren().add(o31);

        body.add(o3);

        ArrayJsonType  o4 = new ArrayJsonType();
        //o4.setArrayItemType(SimpleParamType.STRING.typeName());
        o4.setDesc("类目id集合对象");
        o4.setRequired(false);
        o4.setName("categoryIds");


        JsonType o41 = new SimpleJsonType(SimpleParamType.LONG,"categoryId");
        o41.setRequired(true);
        o41.setValue(33l);
        o41.setDesc("类目id");

        o4.addChild(o41);

        body.add(o4);


        input.setBody(body);
        /*
    String name;

    Object value;

    String className;

    String desc;

    boolean required;

    String type;

    List<JsonType> children = new LinkedList<>();
    */


        WebServiceOutputDTO output = new WebServiceOutputDTO();


        List<JsonType> respBody = new ArrayList<>();



        ObjectJsonType o5 = new ObjectJsonType();
        o5.setRequired(true);
        o5.setName("skuInfo");
        o5.setDesc("商品详情");
        o5.setClassName("com.jd.console.User");


        JsonType o51 = new SimpleJsonType(SimpleParamType.LONG,"skuId");
        o51.setValue(66666l);
        o51.setRequired(true);
        o51.setDesc("商品skuId");
        o5.getChildren().add(o51);

        JsonType o52 = new SimpleJsonType(SimpleParamType.STRING,"skuName");
        o52.setRequired(true);
        o52.setValue("iphone13");
        o52.setDesc("商品名称");
        o5.getChildren().add(o52);
        respBody.add(o5);
        output.setBody(respBody);

        dto.setInput(input);
        dto.setOutput(output);

        System.out.println(JsonUtils.toJSONString(dto));


        CommonResult result1 = new CommonResult(0, "成功", dto);
        System.out.println(JsonUtils.toJSONString(result1));

        CommonResult result = new CommonResult(0, "成功", "<aaa>responsee</aaaa>");

        System.out.println(JsonUtils.toJSONString(result));


        List<ConvertWebServiceBaseDto> list = new ArrayList<>();

        ConvertWebServiceBaseDto obj = new ConvertWebServiceBaseDto();
        list.add(obj);
        result.setData(list);
        obj.setInterfaceId(1l);
        obj.setMethodId(1l);
        obj.setId(1l);
        obj.setPublished(0);
        obj.setCallEnv("测试环境");
        obj.setModified(new Date());
        System.out.println(JsonUtils.toJSONString(result));

        CallHttpToWebServiceReqDTO a  = new CallHttpToWebServiceReqDTO();
        a.setMethodId(1l);
        a.setInterfaceId(1l);
        a.setEndpointUrl("http://endpointurl");
        a.setInputType("xml");
        a.setInput("input value");


        System.out.println(JsonUtils.toJSONString(a));

    }
}
