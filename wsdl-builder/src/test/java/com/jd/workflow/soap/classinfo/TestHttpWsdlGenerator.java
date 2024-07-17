package com.jd.workflow.soap.classinfo;


import com.jd.workflow.soap.BaseTestCase;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.wsdl.HttpDefinition;
import com.jd.workflow.soap.wsdl.HttpWsdlGenerator;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
@Slf4j
public class TestHttpWsdlGenerator extends BaseTestCase {

    List<JsonType> loadJsonType(String filePath){
        try {
            File file = ResourceUtils.getFile(filePath);
            String content = IOUtils.toString(new FileInputStream(file));
            Map<String,Object> result =   JsonUtils.parse(content,Map.class);
            List ret = JsonTypeUtils.jsonStringTypeToJson(result);

            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }
    @Test
    public void testToJsonType(){
        List<JsonType> jsonTypes = loadJsonType("classpath:json/Person.json");
        ObjectJsonType jsonType = new ObjectJsonType();
        for (JsonType type : jsonTypes) {
            jsonType.addChild(type);
        }
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
    SimpleJsonType newSimpleType(String name,String type){
        SimpleJsonType simpleJsonType  = new SimpleJsonType();
        simpleJsonType.setName(name);
        simpleJsonType.setType(type);
        return simpleJsonType;
    }
    @Test(expected = BizException.class)
    public void testVarName() throws Exception {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("你好");
        definition.setBody(loadJsonType("classpath:json/Person.json"));
        definition.setRespBody(loadJsonType("classpath:json/Person.json"));
        definition.setHeaders(Collections.singletonList(newSimpleType("id","long")));
        definition.setParams(Collections.singletonList(newSimpleType("id","long")));
        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
       // validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);
        //validateIsValidWsdl(result);
    }
    @Test
    public void testArray() throws Exception {
        String content = getResourceContent("classpath:json/http-array-plain-type.json");
        HttpDefinition definition = JsonUtils.parse(content,HttpDefinition.class);

        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
         validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);
        //validateIsValidWsdl(result);
    }
    @Test
    public void testArrayItemNoName() throws Exception {
        String content = getResourceContent("classpath:json/http-array-item-no-name.json");
        HttpDefinition definition = JsonUtils.parse(content,HttpDefinition.class);

        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
        validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);

    }
    @Test
    public void testAllNode() throws Exception {
        String content = getResourceContent("classpath:json/http-all-node.json");
        HttpDefinition definition = JsonUtils.parse(content,HttpDefinition.class);

        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
        validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);

    }
    @Test
    public void testFormType() throws Exception {
        String content = getResourceContent("classpath:json/http-form-data.json");
        HttpDefinition definition = JsonUtils.parse(content,HttpDefinition.class);

        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
        validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);

    }
    @Test
    public void testArraySimpleJson() throws Exception {
        String content = getResourceContent("classpath:json/http-array-simple-json.json");
        HttpDefinition definition = JsonUtils.parse(content,HttpDefinition.class);

        Definition wsdlDefinition = HttpWsdlGenerator.generateWsdlDefinition(definition);
        validate(wsdlDefinition,definition);
        log.info("----------------------------------------------");
        log.info("result={}",wsdlDefinition);
        //validateIsValidWsdl(result);
    }
    @Test(expected = StdException.class)
    public void testValidateClassName() throws WSDLException {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("getPerson");
        List<JsonType> jsonTypes = loadJsonType("classpath:json/Person.json");
        jsonTypes.get(0).setClassName("测试信息");
        definition.setBody(jsonTypes);
        definition.setRespBody(jsonTypes);
        definition.setHeaders(Collections.singletonList(newSimpleType("id","long")));
        definition.setParams(Collections.singletonList(newSimpleType("id","long")));
        String result = HttpWsdlGenerator.generateWsdl(definition);
        log.info("----------------------------------------------");
        log.info("result={}",result);
        validateIsValidWsdl(result);
    }
    public void validate(Definition wsdlDefinition,HttpDefinition httpDefinition) throws Exception {
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(wsdlDefinition);
        SoapMessageBuilder messageBuilder = new SoapMessageBuilder(wsdlDefinition);
        for (Object o : wsdlDefinition.getBindings().entrySet()) {
            Map.Entry<QName,Binding> entry = (Map.Entry<QName, Binding>) o;
            BindingOperation operation = null;
            for (Object bindingOperation : entry.getValue().getBindingOperations()) {
                operation = (BindingOperation) bindingOperation;
            }
            JsonType req = transformer.buildSoapMessageFromInput(entry.getValue(), operation, SoapContext.DEFAULT).toJsonType();
            JsonType resp = transformer.buildSoapMessageFromOutput(entry.getValue(), operation, SoapContext.DEFAULT).toJsonType();

            String input = messageBuilder.buildSoapMessageFromInput(entry.getValue() , operation, SoapContext.DEFAULT);
            String output =  messageBuilder.buildSoapMessageFromOutput(entry.getValue(), operation,SoapContext.DEFAULT);
            log.info("soap.input={},out={}",input,output);
            log.info("soap.inputJson={},outJson={}",JsonUtils.toJSONString(req),JsonUtils.toJSONString(resp));
            validate(req,resp,httpDefinition);
        }
    }
    public void validate(JsonType envelopReq,JsonType resp,HttpDefinition definition){
        JsonType envelopOpType = JsonTypeUtils.getEnvelopOpType(envelopReq);
        JsonType headers = JsonTypeUtils.get(envelopOpType, "headers");
        JsonType body = null;
        if("json".equals(definition.getReqType())){

            body = JsonTypeUtils.get(envelopOpType, "body");
        }else{
            body = JsonTypeUtils.get(envelopOpType, "body","root");
        }

        JsonType params = JsonTypeUtils.get(envelopOpType, "params");
        JsonType pathParams = JsonTypeUtils.get(envelopOpType, "pathParams");
        assertJsonTypeEquals(headers,definition.getHeaders());
        assertJsonTypeEquals(params,definition.getParams());
        assertJsonTypeEquals(body,definition.getBody());
        assertJsonTypeEquals(pathParams,definition.getPath());

        JsonType returnType = JsonTypeUtils.getHttp2WsRespReturnType(resp);
        JsonType respHeaders = JsonTypeUtils.get(returnType, "headers");
        JsonType respBody = JsonTypeUtils.get(returnType, "body");

        assertJsonTypeEquals(respHeaders,definition.getRespHeaders());
        assertJsonTypeEquals(respBody,definition.getRespBody());
    }
    private static boolean isNameEquals(String name,String name2){
        String name11 = StringHelper.camelParamName(name).toLowerCase();
        String name22 = StringHelper.camelParamName(name2).toLowerCase();
        return name11.equals(name22);
    }
    public void assertJsonTypeEquals(JsonType jsonType1,List<? extends JsonType> jsonType2){
        if(jsonType1 == null){
            if(  !CollectionUtils.isEmpty(jsonType2))
               throw new StdException("not match");
            return;
        }
        if(jsonType1 instanceof SimpleJsonType){
            return;
        }

        assertJsonTypeEquals(((ObjectJsonType)jsonType1).getChildren(),jsonType2,false);
    }

    /**
     *
     * @param jsonType1
     * @param jsonType2
     * @param ignoreCurrentName 数组元素不要求当前字段名一致
     */
    public void assertJsonTypeEquals(JsonType jsonType1,JsonType jsonType2,boolean ignoreCurrentName){
        if(jsonType1 == null || jsonType2 == null){
            throw new StdException("not match");
        }
        if(!isNameEquals(jsonType1.getName(),jsonType2.getName()) && !ignoreCurrentName){
            throw new StdException("not match").param("name",jsonType1.getName());
        }
        if(jsonType1 instanceof SimpleJsonType   && jsonType2 instanceof ComplexJsonType
        ){
            if(((ComplexJsonType) jsonType2).getChildren().isEmpty()) return;
        }
        if(jsonType1 instanceof ComplexJsonType   && jsonType2 instanceof SimpleJsonType
        ){
            if(((ComplexJsonType) jsonType1).getChildren().isEmpty()) return;
        }
        if(!jsonType1.getClass().getName().equalsIgnoreCase(jsonType2.getClass().getName())) {
            throw new StdException("not match").param("name",jsonType1.getName());
        }
        if(jsonType1 instanceof ComplexJsonType){
            boolean isArray = jsonType1 instanceof ArrayJsonType;
            List<JsonType> children1 = ((ComplexJsonType) jsonType1).getChildren();
            List<JsonType> children2 = ((ComplexJsonType) jsonType2).getChildren();
            assertJsonTypeEquals(children1, children2,isArray);
        }

    }
    public void assertJsonTypeEquals(List<? extends JsonType> jsonType1,List<? extends JsonType> jsonType2,boolean isArray){
        if(jsonType1 == null || jsonType2 == null){
            throw new StdException("not match");
        }
        if(jsonType1.size() != jsonType2.size()){
            throw new StdException("not match").param("name",jsonType1.size());
        }
        int index = -1;
        for (JsonType jsonType : jsonType1) {
            index++;
            boolean found = false;
            if(isArray){
                assertJsonTypeEquals(jsonType2.get(index),jsonType,true);
                continue;
            }
            for (JsonType type : jsonType2) {
                if(isMatch(jsonType.getName(),type.getName())){
                    found = true;
                    assertJsonTypeEquals(type,jsonType,false);
                }
            }
            if(!found){
                throw new StdException("not found elm").param("name",jsonType.getName());
            }
        }
    }
    @Test
    public void testGenerator() throws WSDLException {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("getPerson");
        definition.setBody(loadJsonType("classpath:json/Person.json"));
        definition.setRespBody(loadJsonType("classpath:json/Person.json"));
        definition.setHeaders(Collections.singletonList(newSimpleType("id","long")));
        definition.setParams(Collections.singletonList(newSimpleType("id","long")));
        String result = HttpWsdlGenerator.generateWsdl(definition);
        log.info("----------------------------------------------");
        log.info("result={}",result);
        validateIsValidWsdl(result);
    }
    @Test
    public void testSetRawName() throws Exception {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("getPerson");
        definition.setBody(loadJsonType("classpath:json/Person-keys.json"));
        definition.setRespBody(loadJsonType("classpath:json/Person-keys.json"));
        definition.setRespHeaders(Collections.singletonList(newSimpleType("x_id","long")));
        definition.setHeaders(Collections.singletonList(newSimpleType("x-id","long")));
        definition.setParams(Collections.singletonList(newSimpleType("x-id_d","long")));
        Definition def = HttpWsdlGenerator.generateWsdlDefinition(definition);
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(def);

        for (Object o : def.getBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            BindingOperation getPerson = entry.getValue().getBindingOperation("getPerson", null, null);
            JsonType input = transformer.buildSoapMessageFromInput(entry.getValue(), getPerson, SoapContext.DEFAULT).toJsonType();
            HttpWsdlGenerator.setReqRawName(input,definition);
            JsonType output = transformer.buildSoapMessageFromOutput(entry.getValue(), getPerson, SoapContext.DEFAULT).toJsonType();
            HttpWsdlGenerator.setRespRawName(output,definition);
            log.info("inputJson={}",JsonUtils.toJSONString(input));
            log.info("outputJson={}",JsonUtils.toJSONString(output));
        }
    }
    static void assertRawNameEquals(JsonType raw,JsonType jsonType){
        String name = raw.getRawName();
        if(name == null){
            name = raw.getName();
        }
        if(!name.equals(jsonType.getName())) {
            throw new RuntimeException("不匹配:"+raw.getName());
        }
        List<JsonType> children1 = null;
        List<JsonType> children2 = null;

        if(raw instanceof ObjectJsonType ){
            children1 = ((ObjectJsonType) raw).getChildren();
            children2 = ((ObjectJsonType) jsonType).getChildren();
        }else if(raw instanceof ArrayJsonType ){
            children1 = ((ArrayJsonType) raw).getChildren();
            children2 = ((ArrayJsonType) jsonType).getChildren();
        }
        assertRawNameEquals(children1,children2);

    }
    private static boolean isMatch(String name,String name2){
        String name11 = StringHelper.camelParamName(name).toLowerCase();
        String name22 = StringHelper.camelParamName(name2).toLowerCase();
        return name11.equals(name22);
    }
    static void assertRawNameEquals(List<? extends JsonType> rawTypes, List<? extends JsonType> originalTypes){
        for (JsonType jsonType : rawTypes) {
            for (JsonType type : originalTypes) {
                if(isMatch(jsonType.getName(),type.getName())){
                    assertRawNameEquals(jsonType,type);
                }
            }
        }
    }
    /**
     * 测试只有输出的生成值是否正确
     */
    @Test
    public void testOnlyOutputGenerator() throws WSDLException {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("getPerson");
        definition.setRespBody(loadJsonType("classpath:json/Person.json"));
        String result = HttpWsdlGenerator.generateWsdl(definition);
        log.info("----------------------------------------------");
        log.info("result={}",result);
        validateIsValidWsdl(result);
    }
    /**
     * 测试只有输出的生成值是否正确
     */
    @Test
    public void testAllNullGenerator() throws WSDLException {
        HttpDefinition definition = new HttpDefinition();
        definition.setPkgName("com.wjf");
        definition.setMethodName("getPerson");

        String result = HttpWsdlGenerator.generateWsdl(definition);
        log.info("----------------------------------------------");
        log.info("result={}",result);
        validateIsValidWsdl(result);
    }

    boolean validateIsValidWsdl(String wsdlContent){
        Definition definition = WsdlUtils.parseWsdlByContent(wsdlContent);
        return true;
    }
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("name",123);
        Map<String,Object> childMap = new HashMap<>();
        childMap.put("child",3);
        map.put("child",childMap);
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(childMap);
        map.put("list",list);
        System.out.println(StringHelper.encodeQuery(map,"utf-8"));
    }
}
