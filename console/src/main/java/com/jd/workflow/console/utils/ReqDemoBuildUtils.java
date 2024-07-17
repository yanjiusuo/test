package com.jd.workflow.console.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.mock.HttpDemoValue;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.ValueBuilderAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReqDemoBuildUtils {
    public static final String SPLITE = ": ";
    public static final String NEW_LINE = "\n";
    static ParametersUtils utils = new ParametersUtils();

    final static Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("integer", "number");
        TYPE_MAP.put("string", "string");
        TYPE_MAP.put("date", "string");
        TYPE_MAP.put("datetime", "string");
        TYPE_MAP.put("bigdecimal", "number");
        TYPE_MAP.put("long", "number");
        TYPE_MAP.put("double", "number");
        TYPE_MAP.put("float", "number");
        TYPE_MAP.put("boolean", "boolean");

    }

    public static String getJsfOutputDemoValue(JsfStepMetadata jsfStepMetadata) {
        if (jsfStepMetadata.getOutput() == null) return null;

        try{
            return JsonUtils.toJSONString(buildDemoValue(jsfStepMetadata.getOutput(),false));
        }catch (Exception e){
            log.error("jsf.err_get_output_value",e);
            return null;
        }

    }
    private static boolean hasMockExpr(JsonType jsonType){
        return jsonType != null && StringUtils.isNotBlank(jsonType.getMock()) &&
                !jsonType.getMock().contains("@");
    }
    private static boolean hasMockExpr(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return false;
        for(JsonType jsonType:jsonTypes){
            if(hasMockExpr(jsonType)) return true;
        }
        return false;
    }
    private static boolean hasMockExpr(HttpMethodModel methodModel){
        return hasMockExpr(methodModel.getInput().getHeaders()) ||
                hasMockExpr(methodModel.getInput().getParams()) ||
                hasMockExpr(methodModel.getInput().getPath()) ||
                hasMockExpr(methodModel.getInput().getBody()) ||
                hasMockExpr(methodModel.getOutput().getHeaders()) ||
                hasMockExpr(methodModel.getOutput().getBody());
    }





    public static void replaceMapNullValue(Map map){
        if(map.containsKey(null)){
            Object nullValue = map.remove(null);
            map.put("",nullValue);
        }

    }
    public static Object getJsfInputExampleValue(JsfStepMetadata jsfStepMetadata) {
        List paramValue = new ArrayList();
        for (JsonType jsonType : jsfStepMetadata.getInput()) {

            try{
                final Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
                    @Override
                    public Object afterSetValue(Object value, JsonType jsonType) {
                        if(value instanceof Map){
                            replaceMapNullValue((Map)value);
                        }
                        if (jsonType instanceof SimpleJsonType && "string".equals(jsonType.getType())) {
                            return "";
                        }
                        return value;
                    }
                });
                paramValue.add(value);
            }catch (Exception e){
                paramValue.add(null);
                log.error("jsf.err_convert_input_value",e);
            }

        }
        return paramValue;
    }

    public static String getJsfInputDemoValue(JsfStepMetadata jsfStepMetadata) {
        List paramValue = new ArrayList();
        for (JsonType jsonType : jsfStepMetadata.getInput()) {
            try {
                paramValue.add(buildDemoValue(jsonType,false));
            }catch(Exception e){
                paramValue.add(null);
                log.error("jsf.err_convert_input_value",e);
            }

        }
        removeClassProp(paramValue);

        String ret = JsonUtils.toJSONString(paramValue);
        // 去掉前后2个参数
        return ret;// ret.substring(1, ret.length() - 1);
    }

    public static Object buildDemoValue(JsonType jsonType,boolean convert){

        try {
             Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
                @Override
                public Object afterSetValue(Object value, JsonType jsonType) {
                    final Object demoValue = JsfParamConverterRegistry.buildDemoValue(jsonType);
                    if (demoValue != null) return demoValue;
                    return value;
                }
            });
             if(convert){
                 return JsfParamConverterRegistry.convertValue(jsonType, value);
             }
           return value;
        }catch(Exception e){
            log.error("jsf.err_convert_input_value",e);
            return null;
        }
    }

    /*
       移除map类型里的class值
    */
    public static void removeClassProp(Object value) {
        if (value == null) return;
        if (value instanceof Map) {
            ((Map) (value)).remove("class");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                removeClassProp(entry.getValue());
            }
        } else if (value instanceof List) {
            for (Object val : (List) value) {
                removeClassProp(val);
            }
        }
    }

    static Map<String, Object> buildInput(List<? extends JsonType> jsonTypes) {
        if (jsonTypes == null) return null;
        if(jsonTypes == null
                || jsonTypes.isEmpty()
        ) return null;
        Map<String,Object> args = new HashMap<>();
        for (JsonType inputParam : jsonTypes) {
            if(null!=inputParam){
                try {
                    args.put(inputParam.getName(),buildDemoValue(inputParam,false));
                } catch (Exception e) {
                    log.info("测试inputParam异常",e);
                }
            }
        }
        return args;

    }
     public static String buildHttpInputDemoValue(HttpMethodModel methodModel,HttpDemoValue httpDemoValue,String functionId){

        //color 请求示例处理
         if (StringUtils.isNotEmpty(functionId)) {
             String body = "{}";
             StringBuilder paramsSb = new StringBuilder();
             if (!ObjectHelper.isEmpty(methodModel.getInput().getParams())) {
                 for (JsonType item : methodModel.getInput().getParams()) {
                     paramsSb.append("&" + item.getName() + "=" + item.getValue());
                 }
             }
             String colorExample = "";
             if ("form".equals(methodModel.getInput().getReqType())) {
                 if (!ObjectHelper.isEmpty(methodModel.getInput().getBody())) {
                     Object bodydata = ((HashMap) httpDemoValue.getInputBody()).get("body");
                     body = JsonUtils.toJSONString(bodydata);
                 }
                 colorExample = "curl --location -g --request GET 'https://pre-api.m.jd.com?&appid=XXX&client=XXX&body="
                         + body + paramsSb + "' --header 'Referer: http://www.jd.com'";
             } else if ("json".equals(methodModel.getInput().getReqType())) {
                 if (!ObjectHelper.isEmpty(methodModel.getInput().getBody())) {
                     body = JSONObject.toJSONString(httpDemoValue.getInputBody());
                 }
                 colorExample = "curl -X POST -H \"Content-Type: application/json\" -d '" + body + "' https://pre-api.m.jd.com?functionId=XXX&appid=XXX" + paramsSb.toString();
             }
             return colorExample;
         }
         Map<String, Object> reqBody = new HashMap<>();
         if (!ObjectHelper.isEmpty(methodModel.getInput().getHeaders())) {
             reqBody.put("headers", httpDemoValue.getInputHeaders());
         }
         if (!ObjectHelper.isEmpty(methodModel.getInput().getParams())) {
             reqBody.put("params", httpDemoValue.getInputParams());
         }
         if(!ObjectHelper.isEmpty(methodModel.getInput().getPath())){
             reqBody.put("path",httpDemoValue.getInputPath());
         }
         ReqType reqType = ReqType.json;
         if (!StringUtils.isBlank(methodModel.getInput().getReqType())) {
             reqType = ReqType.valueOf(methodModel.getInput().getReqType());
         }
         if (!CollectionUtils.isEmpty(methodModel.getInput().getBody())) {
             if (ReqType.json.equals(reqType)) {
                 if(methodModel.getInput().getBody().get(0) != null){
                     reqBody.put("body",httpDemoValue.getInputBody());
                 }
             } else {
                 reqBody.put("body", httpDemoValue.getInputBody());
             }
         }
         return JsonUtils.toJSONString(reqBody);
     }
    public static String buildHttpInput(HttpMethodModel methodModel) {

        Map<String, Object> reqBody = new HashMap<>();
        if (methodModel == null) return "";
        if (!ObjectHelper.isEmpty(methodModel.getInput().getHeaders())) {
            reqBody.put("headers", buildInput(methodModel.getInput().getHeaders()));
        }
        if (!ObjectHelper.isEmpty(methodModel.getInput().getParams())) {
            reqBody.put("params", buildInput(methodModel.getInput().getParams()));
        }
        if(!ObjectHelper.isEmpty(methodModel.getInput().getPath())){
            reqBody.put("path",buildInput(methodModel.getInput().getPath()));
        }
        ReqType reqType = ReqType.json;
        if (!StringUtils.isBlank(methodModel.getInput().getReqType())) {
            reqType = ReqType.valueOf(methodModel.getInput().getReqType());
        }
        if (!CollectionUtils.isEmpty(methodModel.getInput().getBody())) {
            if (ReqType.json.equals(reqType)) {
                if(methodModel.getInput().getBody().get(0) != null){
                    reqBody.put("body",buildDemoValue(methodModel.getInput().getBody().get(0),false));
                }
            } else {
                reqBody.put("body", buildInput(methodModel.getInput().getBody()));
            }
        }
        return JsonUtils.toJSONString(reqBody);
    }

    public static String buildHttpOutput(HttpMethodModel methodModel) {
        String responseBody = null;
        if (methodModel.getOutput() != null && !CollectionUtils.isEmpty(methodModel.getOutput().getBody())) {
            JsonType rootJsonType = methodModel.getOutput().getBody().get(0);
            if(rootJsonType != null){
                responseBody = JsonUtils.toJSONString(buildDemoValue(rootJsonType,false));
            }

        }
        return responseBody;
    }

    public static String buildInputTypeScript(String content) {
        StringBuilder result = new StringBuilder();
        com.alibaba.fastjson.JSONObject contentObj = com.alibaba.fastjson.JSON.parseObject(content);
        if (contentObj.containsKey("input")) {
            com.alibaba.fastjson.JSONObject inputObj = contentObj.getJSONObject("input");
            if (inputObj.containsKey("body")) {
                com.alibaba.fastjson.JSONArray bodyArr = inputObj.getJSONArray("body");
                if (bodyArr != null && bodyArr.size() > 0) {
                    return getObjectString(bodyArr.getJSONObject(0),true);
                }
            }
        }
        result.append(NEW_LINE);

        return result.toString();
    }

    public static String buildOutputTypeScript(String content) {
        StringBuilder result = new StringBuilder();
        com.alibaba.fastjson.JSONObject contentObj = com.alibaba.fastjson.JSON.parseObject(content);
        if (contentObj.containsKey("output")) {
            com.alibaba.fastjson.JSONObject inputObj = contentObj.getJSONObject("output");
            if (inputObj.containsKey("body")) {
                com.alibaba.fastjson.JSONArray bodyArr = inputObj.getJSONArray("body");
                if (bodyArr != null && bodyArr.size() > 0) {
                    return getObjectString(bodyArr.getJSONObject(0),true);
                }
            }
        }

        result.append(NEW_LINE);
        return result.toString();
    }

    public static String getObjectString(com.alibaba.fastjson.JSONObject jsonObject,boolean skipName) {
        StringBuilder result = new StringBuilder();
        if ("object".equals(jsonObject.getString("type"))) {
            if(!skipName){

                result.append(jsonObject.getString("name"));
                result.append(SPLITE);
            }


            result.append("{");
            result.append(NEW_LINE);
            JSONArray children = jsonObject.getJSONArray("children");
            if (children != null && children.size() > 0) {
                for (int i = 0; i < jsonObject.getJSONArray("children").size(); i++) {
                    result.append(getObjectString(jsonObject.getJSONArray("children").getJSONObject(i),false));
                    result.append(NEW_LINE);
                }
            }
            result.append("}");
        }
        else if("array".equals(jsonObject.getString("type"))){
            if(!skipName){
                result.append(jsonObject.getString("name"));
                result.append(SPLITE);
            }


            //result.append(NEW_LINE);
            JSONArray children = jsonObject.getJSONArray("children");
            if (children != null && children.size() ==1  ) {
                for (int i = 0; i < jsonObject.getJSONArray("children").size(); i++) {
                    result.append(getObjectString(jsonObject.getJSONArray("children").getJSONObject(i),true));
                    //result.append(NEW_LINE);
                }
            }else{
                result.append("any");
            }
            result.append("[");
            result.append("]");
        }
        else {
            return getBaseObjectString(jsonObject,skipName);
        }
        return result.toString();
    }

    private static String getBaseObjectString(JSONObject jsonObject,boolean skipName) {
        StringBuilder result = new StringBuilder();
        if(!skipName){
            result.append(jsonObject.getString("name"));
            result.append(SPLITE);
        }
        if (TYPE_MAP.containsKey(jsonObject.getString("type"))) {
            result.append(TYPE_MAP.get(jsonObject.getString("type")));
        } else {
            result.append("object");
        }
        return result.toString();

    }

    public static void main(String[] args) {
        String body = "{\"input\":[{\"name\":\"searchParam\",\"type\":\"object\",\"className\":\"com.jd.rc.app.ka.salestock.soa.b.api.param.SaleStockSearchParam\",\"children\":[{\"name\":\"upcCodes\",\"type\":\"array\",\"desc\":\"商品条码集合\",\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"whIds\",\"type\":\"array\",\"desc\":\"门店编码集合\",\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"long\",\"className\":\"java.lang.Long\"}]},{\"name\":\"skuIds\",\"type\":\"array\",\"desc\":\"商品编码集合\",\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"string\",\"className\":\"java.lang.String\"}]}]},{\"name\":\"baseParam\",\"type\":\"object\",\"className\":\"com.jd.rc.core.common.api.param.BaseParam\",\"children\":[{\"name\":\"operatorPin\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"merchantCode\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"identity\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"requestId\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"tenantId\",\"type\":\"long\",\"className\":\"java.lang.Long\"},{\"name\":\"client\",\"type\":\"integer\",\"className\":\"java.lang.Integer\"}]},{\"name\":\"page\",\"type\":\"object\",\"className\":\"com.jd.rc.core.common.api.page.Page\",\"children\":[{\"name\":\"totalPage\",\"type\":\"long\",\"className\":null},{\"name\":\"pageSize\",\"type\":\"integer\",\"className\":null},{\"name\":\"totalCount\",\"type\":\"long\",\"className\":null},{\"name\":\"list\",\"type\":\"array\",\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"object\",\"className\":null,\"children\":[]}]},{\"name\":\"pageNum\",\"type\":\"integer\",\"className\":null}]}],\"output\":{\"name\":\"root\",\"type\":\"object\",\"className\":\"com.jd.rc.core.common.api.dto.Response\",\"children\":[{\"name\":\"msg\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"traceId\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"code\",\"type\":\"integer\",\"className\":\"java.lang.Integer\"},{\"name\":\"data\",\"type\":\"object\",\"className\":\"com.jd.rc.core.common.api.page.Page\",\"children\":[{\"name\":\"totalPage\",\"type\":\"long\",\"className\":null},{\"name\":\"pageSize\",\"type\":\"integer\",\"className\":null},{\"name\":\"totalCount\",\"type\":\"long\",\"className\":null},{\"name\":\"list\",\"type\":\"array\",\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"object\",\"className\":\"com.jd.rc.app.ka.salestock.soa.b.api.dto.SaleStockRespDTO\",\"children\":[{\"name\":\"skuName\",\"type\":\"string\",\"desc\":\"商品名称\",\"className\":\"java.lang.String\"},{\"name\":\"qtySafe\",\"type\":\"string\",\"desc\":\"安全库存\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"merchantCode\",\"type\":\"string\",\"desc\":\"商铺编码\",\"className\":\"java.lang.String\"},{\"name\":\"whName\",\"type\":\"string\",\"desc\":\"门店名称\",\"className\":\"java.lang.String\"},{\"name\":\"qtyAvailable\",\"type\":\"string\",\"desc\":\"可用库存\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"qty\",\"type\":\"string\",\"desc\":\"库存总数\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"qtyHold\",\"type\":\"string\",\"desc\":\"订单预占\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"tenantId\",\"type\":\"long\",\"desc\":\"租户编码\",\"className\":\"java.lang.Long\"},{\"name\":\"whType\",\"type\":\"integer\",\"desc\":\"门店类型\",\"className\":\"java.lang.Integer\"},{\"name\":\"whId\",\"type\":\"long\",\"desc\":\"门店编码\",\"className\":\"java.lang.Long\"},{\"name\":\"skuId\",\"type\":\"string\",\"desc\":\"商品编码\",\"className\":\"java.lang.String\"},{\"name\":\"upcCode\",\"type\":\"string\",\"desc\":\"商品条码\",\"className\":\"java.lang.String\"}]}]},{\"name\":\"pageNum\",\"type\":\"integer\",\"className\":null}]}]},\"interfaceName\":\"com.jd.rc.app.ka.salestock.soa.b.api.service.SaleStockSoaBProvider\",\"methodName\":\"selectSaleStock\",\"exceptions\":null,\"cnName\":null,\"desc\":\"销售库存查询\"}";
        JsfStepMetadata metadata = JsonUtils.parse(body,JsfStepMetadata.class);
        String jsfInputDemoValue = ReqDemoBuildUtils.getJsfInputDemoValue(metadata);
        String jsfOutputDemoValue = ReqDemoBuildUtils.getJsfOutputDemoValue(metadata);
        System.out.println(123);
    }

}
