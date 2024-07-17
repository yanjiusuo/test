package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.*;

/**
 * webservice解析后存储到数据库的结构
  存储到数据库的格式：
 {
    input:{
        demoXml:'',
        schemaType:{}
    },
    output:{
      demoXml:'',
      schemaType:{}
    }
 }
 返回给前端的格式：
 {
 input:{
    header:List<JsonType>
    body:List<JsonType>
 },
 output:{
     header:List<JsonType>
     body:List<JsonType>
 }
 }
 */
@Data
public class WebServiceStorageMetadata extends StepMetadata {
    StorageData input;
    StorageData output;



    /**
     * 给前端的返回值格式,schemaType里只有header与body对象
     */
    public Map<String,Object>  toFront(){
        Map<String,Object> result = new HashMap<>();
        result.put("input",input.toFront());
        result.put("output",output.toFront());
        return result;
    }
    public void init(){
        input.init();
        output.init();
    }
   @Data
   public static class StorageData {
       /**
        * 示例的xml报文
        */
       String demoXml;
       /**
        * schemaType报文
        */
       JsonType schemaType;



       public void init(){
        }


       public Object toFront() {
           Map<String,Object> map = new HashMap<>();
           map.put("demoXml",demoXml);
           Map<String,Object> transformedSchemaTypes = new LinkedHashMap<>();
           JsonType header = JsonTypeUtils.get(schemaType, "Header");
           JsonType body = JsonTypeUtils.get(schemaType, "Body");
           Map<String, Object> headerMap = header.toJson();
           Map<String, Object> bodyMap = header.toJson();

           transformedSchemaTypes.put("header",headerMap.get("children"));
           transformedSchemaTypes.put("body",bodyMap.get("children"));
           map.put("schemaType", schemaType);
           return map;
       }
   }

}
