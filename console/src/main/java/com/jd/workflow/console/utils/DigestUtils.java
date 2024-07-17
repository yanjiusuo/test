package com.jd.workflow.console.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.Md5Utils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.RefObjectJsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DigestUtils {
    public static List<String> defaultNeedDeltaAttrs = Arrays.asList("desc","required","value","name","type");
    public static List<String> structNeedDeltaAttrs = Arrays.asList("name","type");
    static ObjectMapper objectMapper = new ObjectMapper();
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static {
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, false);
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);



        //忽略空Bean转json的错误
        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
    }
    private static MethodMetadata toClassMetadata(JsfStepMetadata method){
        MethodMetadata metadata = new MethodMetadata();
        metadata.setInterfaceName(method.getInterfaceName());
        metadata.setMethodName(method.getMethodName());
        metadata.setInput(method.getInput());
        metadata.setOutput(method.getOutput());
        metadata.setExceptions(method.getExceptions());
        return metadata;
    }

    public static String getJsfMethodMd5(IMethodInfo jsfMethod,JsfStepMetadata metadata){
        MethodMetadata methodMetadata = toClassMetadata(metadata);
        methodMetadata.setDesc(jsfMethod.getDesc());
        return getJsfMethodMd5(jsfMethod,methodMetadata);
    }
    private static StringBuilder getJsfMethodStr(MethodMetadata metadata,List<String> needAttrs){
        StringBuilder sb = new StringBuilder();
        sb.append(JsonUtils.toJSONString(jsonTypeList(metadata.getInput(),needAttrs)));
        sb.append(JsonUtils.toJSONString(jsonTypeMap(metadata.getOutput(),needAttrs)));
        return sb;
    }
    public static String getJsfMethodMd5(IMethodInfo jsfMethod,MethodMetadata metadata){
        StringBuilder sb = getJsfMethodStr(metadata,structNeedDeltaAttrs);

        MethodDigest digest = new MethodDigest();

        StringBuilder contentSb = getJsfMethodStr(metadata,defaultNeedDeltaAttrs);
        if(jsfMethod != null){
            if(StringUtils.isNotBlank(jsfMethod.getDesc() )){
                contentSb.append( jsfMethod.getDesc() );
            }
        }
        digest.setContentDigest(Md5Utils.md5(contentSb.toString()));
        digest.setStructDigest(Md5Utils.md5(sb.toString()));
        return digest.toString() ;
    }
    public static String getJsfMethodMd5(MethodMetadata metadata){
       return getJsfMethodMd5(null,metadata);
    }

    /*public static String getJsfMethodMd5(JsfStepMetadata metadata){
        StringBuilder sb = new StringBuilder();
        sb.append(jsonTypeStructDigestStr(metadata.getInput()));
        sb.append(jsonTypeStructDigestStr(metadata.getOutput()));

        return Md5Utils.md5(sb.toString());
    }*/
    public static String getHttpMethodMd5(HttpMethodModel methodModel){
        return getHttpMethodMd5(null,methodModel);
    }
    private static StringBuilder getHttpMethodStr(HttpMethodModel methodModel, List<String> needAttrs){
        StringBuilder sb = new StringBuilder();
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getInput().getParams(),needAttrs)));
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getInput().getPath(),needAttrs)));
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getInput().getBody(),needAttrs)));
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getInput().getHeaders(),needAttrs)));
        sb.append(methodModel.getInput().getReqType());
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getOutput().getHeaders(),needAttrs)));
        sb.append(JsonUtils.toJSONString(jsonTypeList(methodModel.getOutput().getBody(),needAttrs)));
        return sb;
    }
    public static String getHttpMethodMd5(IMethodInfo httpMethod, HttpMethodModel methodModel){
        StringBuilder structSb = getHttpMethodStr(methodModel,structNeedDeltaAttrs);
        MethodDigest digest = new MethodDigest();
        StringBuilder contentSb = getHttpMethodStr(methodModel,defaultNeedDeltaAttrs);
        if(httpMethod != null){
            contentSb.append(httpMethod.getHttpMethod().toLowerCase());
            if(StringUtils.isNotBlank(httpMethod.getName())){
                contentSb.append(httpMethod.getName());
            }
            if(StringUtils.isNotBlank(httpMethod.getDesc() )){
                contentSb.append(httpMethod.getDesc());
            }
        }

        digest.setContentDigest(Md5Utils.md5(contentSb.toString()));
        digest.setStructDigest(Md5Utils.md5(structSb.toString()));
        return digest.toString();
    }

    public static String getJsonTypeDigest(JsonType jsonType){
        String structSb = JsonUtils.toJSONString(jsonTypeMap(jsonType,structNeedDeltaAttrs));
        String content = JsonUtils.toJSONString(jsonTypeMap(jsonType,defaultNeedDeltaAttrs));
        MethodDigest digest = new MethodDigest();


        digest.setContentDigest(Md5Utils.md5(content));
        digest.setStructDigest(Md5Utils.md5(structSb));
        return digest.toString();
    }


    /**
     * 防止前端默认值影响digest值计算
     * @param val
     * @return
     */
    private static Object defaultValue(String propName,Object val){
        if("type".equals(propName) && "ref".equals(val)){ // 引用字段按object类型处理
            return "object";
        }
        if(val == null) return null;
        if(val instanceof String && StringUtils.isBlank((String)val)) return null;
        if(val instanceof Boolean && Boolean.FALSE.equals(val)) return null;
        return val;
    }


    private static Map<String,Object> jsonTypeMap(JsonType jsonType,List<String> needAttrs){
        Map<String,Object> map = new HashMap<>();
        if(jsonType == null) return map;
        if(jsonType instanceof RefObjectJsonType){
            map.put("refName",((RefObjectJsonType) jsonType).getRefName());
        }
        for (String needDeltaAttr : needAttrs) {
            map.put(needDeltaAttr, defaultValue(needDeltaAttr,BeanTool.getProp(jsonType,needDeltaAttr)));
        }
        if(jsonType instanceof ComplexJsonType){
            map.put("children",jsonTypeList(((ComplexJsonType) jsonType).getChildren(),needAttrs));
        }
        return map;
    }
    private static List<Map<String,Object>> jsonTypeList(List<? extends JsonType> complexChildren,List<String> needAttrs){
        if(complexChildren == null){
            complexChildren = new ArrayList<>();
        }
        return complexChildren.stream().map(vs -> jsonTypeMap(vs,needAttrs)).collect(Collectors.toList());
    }





    public static String toJSONString(Object o) {
        try{
            return objectMapper.writeValueAsString(o);
        }catch (Exception e){
            log.error("json.err_serial_obj:o={}",o,e);
            throw new StdException("json.err_serialize_json",e);
        }

    }

}
