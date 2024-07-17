package com.jd.workflow.console.service.remote;

import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.jsf.JsfCallType;
import com.jd.workflow.console.dto.jsf.JsfDebugData;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.dto.mock.HttpDemoValue;
import com.jd.workflow.console.dto.mock.JsfDemoValue;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IColorGatewayServiceImpl;
import com.jd.workflow.console.service.debug.HttpDebugDataDto;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.method.ColorTypeEnum;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.json.RawString;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MockDataBuildService {
    @Value("${mock.useEasyMockDataBuildService:true}")
    private boolean useEasyMockDataBuildService;
    @Autowired
    EasyMockHttpService easyMockHttpService;

    @Autowired
    IColorGatewayServiceImpl colorGatewayService;

    private  boolean hasMockExpr(JsonType jsonType){
        boolean result = jsonType != null && StringUtils.isNotBlank(jsonType.getMock()) &&
                jsonType.getMock().contains("@");
        if(result) return result;
        if(jsonType instanceof ComplexJsonType){
            if(ObjectHelper.isEmpty(((ComplexJsonType) jsonType).getChildren())) return false;
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                if(hasMockExpr(child)) return true;
            }
        }
        return false;
    }
    private  boolean hasMockExpr(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return false;
        for(JsonType jsonType:jsonTypes){
            if(hasMockExpr(jsonType)) return true;
        }
        return false;
    }
    private  boolean hasMockExpr(HttpMethodModel methodModel){
        return hasMockExpr(methodModel.getInput().getHeaders()) ||
                hasMockExpr(methodModel.getInput().getParams()) ||
                hasMockExpr(methodModel.getInput().getPath()) ||
                hasMockExpr(methodModel.getInput().getBody()) ||
                hasMockExpr(methodModel.getOutput().getHeaders()) ||
                hasMockExpr(methodModel.getOutput().getBody());
    }
    private   boolean hasMockExpr(JsfStepMetadata metadata){
        return hasMockExpr(metadata.getInput()) || hasMockExpr(metadata.getOutput());
    }
    private  Object toMockValue(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return null;
        Map<String,Object> values = new HashMap<>();
        for(JsonType jsonType:jsonTypes){
                    values.put(jsonType.getName(),toMockValue(jsonType));
        }
        return values;
    }
    private  Object toMockValue(JsonType jsonType){
        return buildDemoValue(jsonType,false);
    }
    private  Object toMockTemplate(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return null;
        Map<String,Object> values = new HashMap<>();
        for(JsonType jsonType:jsonTypes){
            values.put(jsonType.getName(),toMockTemplate(jsonType));
        }
        return values;
    }
    private  Object toListTemplate(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return null;
        List values = new ArrayList();
        for(JsonType jsonType:jsonTypes){
            values.add(toMockTemplate(jsonType));
        }
        return values;
    }
    private  Object toListValue(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return null;
        List values = new ArrayList();
        for(JsonType jsonType:jsonTypes){
            values.add(toMockValue(jsonType));
        }
        return values;
    }
    private  Object toMockTemplate(JsonType jsonType){
        try {
            Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
                @Override
                public Object afterSetValue(Object value, JsonType jsonType) {
                    if(jsonType.getMock() != null){
                        return jsonType.getMock();
                    }

                    if(jsonType instanceof SimpleJsonType){
                        return buildDemoValue(jsonType,true);
                    }

                    return value;
                }
            });

            return value;
        }catch(Exception e){
            log.error("jsf.err_convert_input_value",e);
            return null;
        }
    }

    public  HttpDemoValue buildHttpDemoValue(HttpMethodModel httpMethodModel,boolean onlyBuildTemplate){
        return buildHttpDemoValue(httpMethodModel,onlyBuildTemplate,false);
    }
    public  HttpDemoValue buildHttpDemoValue(HttpMethodModel httpMethodModel,boolean onlyBuildTemplate,boolean onlyBuildValue){
        HttpDemoValue httpDemoValue = new HttpDemoValue();
        if("json".equals(httpMethodModel.getInput().getReqType())){
            if(!ObjectHelper.isEmpty(httpMethodModel.getInput().getBody()) ){
                httpDemoValue.setInputBody(toMockValue(httpMethodModel.getInput().getBody().get(0)));
            }
        }else{
            httpDemoValue.setInputBody(toMockValue(httpMethodModel.getInput().getBody()));
        }
        httpDemoValue.setInputHeaders((Map<String, Object>) toMockValue(httpMethodModel.getInput().getHeaders()));
        httpDemoValue.setInputParams((Map<String, Object>) toMockValue(httpMethodModel.getInput().getParams()));
        httpDemoValue.setInputPath((Map<String, Object>) toMockValue(httpMethodModel.getInput().getPath()));
        if(!ObjectHelper.isEmpty(httpMethodModel.getOutput().getBody())){
            httpDemoValue.setOutputBody( toMockValue(httpMethodModel.getOutput().getBody().get(0)));
        }
        httpDemoValue.setOutputHeaders((Map<String, Object>) toMockValue(httpMethodModel.getOutput().getHeaders()));
        if(onlyBuildValue || !useEasyMockDataBuildService) return httpDemoValue;


        if(hasMockExpr(httpMethodModel)){
            if("json".equals(httpMethodModel.getInput().getReqType())){
                if(!ObjectHelper.isEmpty(httpMethodModel.getInput().getBody()) ){
                    httpDemoValue.setInputBody(toMockTemplate(httpMethodModel.getInput().getBody().get(0)));
                }
            }else{
                httpDemoValue.setInputBody(toMockTemplate(httpMethodModel.getInput().getBody()));
            }


            httpDemoValue.setInputHeaders((Map<String, Object>) toMockTemplate(httpMethodModel.getInput().getHeaders()));
            httpDemoValue.setInputParams((Map<String, Object>) toMockTemplate(httpMethodModel.getInput().getParams()));
            httpDemoValue.setInputPath((Map<String, Object>) toMockTemplate(httpMethodModel.getInput().getPath()));
            if(!ObjectHelper.isEmpty(httpMethodModel.getOutput().getBody())){
                httpDemoValue.setOutputBody( toMockTemplate(httpMethodModel.getOutput().getBody().get(0)));
            }

            httpDemoValue.setOutputHeaders((Map<String, Object>) toMockTemplate(httpMethodModel.getOutput().getHeaders()));
            if(onlyBuildTemplate){
                return httpDemoValue;
            }
            HttpDemoValue newValue = easyMockHttpService.buildHttpMockValue(httpDemoValue);
            if(newValue != null) return newValue;
        }

        return httpDemoValue;
    }
    public  Object buildDemoValue(JsonType jsonType,boolean convert){

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
    public  JsfDemoValue buildJsfDemoValue(JsfStepMetadata jsfStepMetadata,boolean onlyBuildTemplate){
        return buildJsfDemoValue(jsfStepMetadata,onlyBuildTemplate,false);
    }
    public  JsfDemoValue buildJsfDemoValue(JsfStepMetadata jsfStepMetadata,boolean onlyBuildTemplate,boolean onlyBuildValue){
        JsfDemoValue value = new JsfDemoValue();
        value.setInputMockValue((List<Object>) toListValue(jsfStepMetadata.getInput()));
        value.setOutputMockValue(toMockValue(jsfStepMetadata.getOutput()));
        if(onlyBuildValue || !useEasyMockDataBuildService){
            return value;
        }
        if(hasMockExpr(jsfStepMetadata)){
            value.setInputMockValue((List<Object>) toListTemplate(jsfStepMetadata.getInput()));
            value.setOutputMockValue( toMockTemplate(jsfStepMetadata.getOutput()));
            if(onlyBuildTemplate){
                return value;
            }
            JsfDemoValue newValue = easyMockHttpService.buildJsfMockValue(value);
            if(newValue != null) return newValue;
        }

        return value;
    }

    /**
     * jsf jar包调用默认值
     * @param jsonType
     * @return
     */
    public Object buildJarJsfCallEmptyValue(JsonType jsonType){
        if(jsonType ==null) return null;
        return jsonType.toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                if(jsonType.getMock() != null){
                    return jsonType.getMock();
                }
                if(jsonType instanceof SimpleJsonType && "string".equals(jsonType.getType())){
                    return  "";
                }
                if(jsonType instanceof ObjectJsonType && ObjectHelper.isEmpty(((ObjectJsonType) jsonType).getChildren())){ // 可能是一个java.lang.Object或者泛型类型，这种没办法识别具体的出入参格式
                    return null;
                }
                if(value instanceof Map){
                    ((Map) value).put("@type",jsonType.getClassName());
                }
                if(value instanceof List &&contains(jsonType.getClassName(),"Set")){
                    String jsonStr = JsonUtils.toJSONString(value);
                    return new RawString("Set"+jsonStr);
                }
                if(jsonType instanceof MapJsonType ||  contains(jsonType.getClassName(),"Map")){
                    return  new RawString("{}");
                }
                if(jsonType instanceof ObjectJsonType || jsonType instanceof ArrayJsonType){

                    return  value;
                }
                return null;
            }
        });
    }
    private boolean contains(String str,String subStr){
        if(str == null) return false;
        return str.contains(subStr);
    }
    public Object buildEmptyJsonValue(JsonType jsonType){
        if(jsonType ==null) return null;
        return jsonType.toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                if(jsonType.getMock() != null){
                    return jsonType.getMock();
                }
                if(jsonType instanceof SimpleJsonType && "string".equals(jsonType.getType())){
                    return  "";
                }
                if(jsonType instanceof MapJsonType||jsonType instanceof ObjectJsonType || jsonType instanceof ArrayJsonType){
                    return  value;
                }
                return null;
            }
        });
    }
    public List<Object> buildEmptyJsonArrayValue(List<? extends JsonType> jsonTypes){
        List<Object> values = new ArrayList<>();
        if(jsonTypes == null) return values;
        for(JsonType jsonType:jsonTypes){
            Map<String,Object> value = new HashMap<>();

            if(jsonType instanceof ObjectJsonType){
                Object arr=buildEmptyJsonValue(jsonType);
                value.putAll(null==arr?new HashMap<>():(Map<String, Object>)arr);
                values.add(value);
            } else if(jsonType instanceof ArrayJsonType){
                Object arr=buildEmptyJsonValue(jsonType);
                values.add(arr==null?new ArrayList<>():arr);
            } else{
//                value.put(jsonType.getName(),buildEmptyJsonValue(jsonType));
                values.add(buildEmptyJsonValue(jsonType));
            }

        }
        return values;
    }
    public Map<String,Object> buildEmptyJsonValue(List<? extends JsonType> jsonTypes){
        Map<String,Object> values = new HashMap<>();
        if(jsonTypes == null) return values;
        for(JsonType jsonType:jsonTypes){
            values.put(jsonType.getName(),buildEmptyJsonValue(jsonType));
        }
        return values;
    }
    public JsfDebugData buildEmptyJsfValue(JsfStepMetadata jsfStepMetadata){
        JsfDebugData jsfDebugData = new JsfDebugData();
        NewJsfDebugDto input = new NewJsfDebugDto();
        jsfDebugData.setInput(input);
        input.setInput(jsfStepMetadata.getInput());

            List inputData = jsfStepMetadata.getInput().stream().map(jsonType -> buildEmptyJsonValue(jsonType)).collect(Collectors.toList());
            input.setInputData(inputData);


        return jsfDebugData;
    }
    public HttpDebugDataDto buildEmptyHttplue(MethodManage method){
        HttpMethodModel httpMethodModel = (HttpMethodModel) method.getContentObject();

        HttpDebugDataDto debugDataDto = new HttpDebugDataDto();
        debugDataDto.setInput(new HttpDebugDataDto.Input());
        debugDataDto.getInput().setParams(buildEmptyJsonValue(httpMethodModel.getInput().getParams()));
        debugDataDto.getInput().setHeaders(buildEmptyJsonValue(httpMethodModel.getInput().getHeaders()));
        debugDataDto.getInput().setPath(buildEmptyJsonValue(httpMethodModel.getInput().getPath()));
        if(method.getFunctionId()!=null){
            debugDataDto.getInput().setColorHeaders(buildEmptyJsonValue(colorGatewayService.queryColorGateParam(ColorTypeEnum.requestHeader.getCode(),null)));
            debugDataDto.getInput().setColorInPutParam(buildEmptyJsonValue(colorGatewayService.queryColorGateParam(ColorTypeEnum.requestParam.getCode(), null)));
        }
        if(ReqType.json.name().equals(httpMethodModel.getInput().getReqType())){
            List<JsonType> body = httpMethodModel.getInput().getBody();
            if (!ObjectHelper.isEmpty(body)) {
                if (InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())) {
                    debugDataDto.getInput().setBody(buildEmptyJsonArrayValue(httpMethodModel.getInput().getBody()));
                }else{
                    debugDataDto.getInput().setBody(buildEmptyJsonValue(httpMethodModel.getInput().getBody().get(0)));
                }
            }
        }else {
            debugDataDto.getInput().setBody(buildEmptyJsonValue(httpMethodModel.getInput().getBody()));
        }

        return debugDataDto;
    }
}
