package com.jd.workflow.soap.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.util.json.CustomModule;
import lombok.Data;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonUtils {
  static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
  static  ObjectMapper objectMapper = new ObjectMapper();
  private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
  static PropertyUtilsBean utilsBean = new PropertyUtilsBean();
  static {
    objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, false);
    objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(new CustomModule());

    //对象的所有字段全部列入
    objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

    //忽略空Bean转json的错误
    //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
    objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
    //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
  }
  public static ObjectMapper mapper(){
    return objectMapper;
  }
  public static Object parse(String text)   {
      text = StringUtils.strip(text);
      if(StringUtils.isEmpty(text)) return null;
      if(text.startsWith("[")) {
        return parse(text,List.class);
      }else if(text.startsWith("{")){
        return parse(text, Map.class);
      }else if(text.startsWith("\"")){
        return parse(text,String.class);
      }else{
        return parse(text,Number.class);
      }

  }
  public static <T> T parse(String text,Class<T> clazz)   {
    if(StringUtils.isEmpty(text)) return null;
    try {
      return objectMapper.readValue(text,clazz);
    } catch (JsonProcessingException e) {
      if(e.getCause() instanceof StdException){
        throw (StdException)e.getCause();
      }
      throw StdException.adapt ("json.err_parse_json",e);
    }
  }

  public static <T> T parse(String text,TypeReference<T> valueTypeRef)   {
    try {
      return objectMapper.readValue(text,valueTypeRef);
    } catch (JsonProcessingException e) {
      if(e.getCause() instanceof StdException){
        throw (StdException)e.getCause();
      }
      throw StdException.adapt ("json.err_parse_json",e);
    }
  }

  public static <T> List<T> parseArray(String text,Class<T> itemClass)   {
    try {

      JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, itemClass);
      List<T> lst =  (List<T>)objectMapper.readValue(text, javaType);
      return lst;
    } catch (JsonProcessingException e) {
      if(e.getCause() instanceof StdException){
        throw (StdException)e.getCause();
      }
      throw StdException.adapt ("json.err_parse_json",e);
    }
  }
  public static String pretty(Object obj)   {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw StdException.adapt ("json.err_parse_json",e);
    }
  }
  public static String tryPretty(String str)   {
    if(StringHelper.isEmpty(str)) return str;
    try {
      Object obj = parse(str);
      return pretty(obj);
    } catch (Exception e) {
      return str;
    }
  }
  public static <T> Map<String,T> parseMap(String text,Class<T> itemClass)   {
    try {
      HashMap<String, T> map = new HashMap<>();
      Map<String,Object> parse = parse(text, Map.class);
      for (String key : parse.keySet()) {
        T item = parse(toJSONString(parse.get(key)), itemClass);
        map.put(key,item);
      }
      return map;
    } catch (Exception e) {
      throw new StdException("json.err_parse_json",e);
    }
  }
  public static Map objectToMap(Object obj){
    if(obj == null )return Collections.emptyMap();
    return objectMapper.convertValue(obj,Map.class);
  }
  public static <T> T cast(Object obj,Class<T> clazz)   {
      if(obj == null) return null;
      return objectMapper.convertValue(obj,clazz);

  }

  /**
   *  java bean解析后有些属性是不在bean里的，可以临时存储一下
   * @return
   */
  public static Map<String,Object> getBeanAnyAttrs(Object javaBean,Map<String,Object> jsonProps){
    assert  javaBean != null;
    try {
      Map<String, Object> props = utilsBean.describe(javaBean);
      Map<String,Object> result = new HashMap<>();
      for (Map.Entry<String, Object> entry : jsonProps.entrySet()) {
         if((!props.containsKey(entry.getKey()))){
           result.put(entry.getKey(),entry.getValue());
         }
      }
      return result;
    } catch (Exception e) {
        logger.error("java.err_describe_bean:bean={}",javaBean,e);
        throw new StdException("java.err_parse_bean",e);
    }

  }

  public static String toJSONString(Object o) {
    try{
      return objectMapper.writeValueAsString(o);
    }catch (Exception e){
      logger.error("json.err_serial_obj:o={}",o,e);
      throw new StdException("json.err_serialize_json",e);
    }
     
  }
  @Data
  static class DataTest{
     JsonStringObject data;
  }
  public static void main(String[] args) {
    DataTest data= new DataTest();
    JsonStringObject jsonStringObject = new JsonStringObject();
    jsonStringObject.put("a",1);
    data.setData(jsonStringObject);
    final Map cast = JsonUtils.cast(data, Map.class);

    System.out.println(cast);

  }
}
