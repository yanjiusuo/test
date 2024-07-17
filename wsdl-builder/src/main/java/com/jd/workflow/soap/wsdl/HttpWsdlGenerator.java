package com.jd.workflow.soap.wsdl;

import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.wsdl.param.Param;
import com.jd.workflow.soap.wsdl.param.ParamType;

import org.apache.commons.lang.StringUtils;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将http定义转换为wsdl描述,基本思路首先转换为的方法签名为：
     public HttpResponse executeHttp(@WebParam(name="body") Person person, @WebParam(name="params") List<HttpDefinition.ParamItem> params, @WebParam(name="headers") List<HttpDefinition.ParamItem>  headers){
        return new HttpResponse();
     }
 其中，httpResponse 结构为:
    List<ParamItem> headers;
    Object body;

 */
public class HttpWsdlGenerator {
    public static String defaultPkgName = "com.jd.test";
    public static String RESULT_NAME = "Result";
    private static void validateClassName(JsonType jsonType){
        String className = jsonType.getClassName();
        if(!StringUtils.isEmpty(className) &&
                !StringHelper.isValidClassName(className)){
            throw new BizException("无效的类名:"+className);
        }
        if(jsonType instanceof ObjectJsonType){
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                validateClassName(child);
            }
        }else if(jsonType instanceof ArrayJsonType){
            for (JsonType child : ((ArrayJsonType) jsonType).getChildren()) {
                validateClassName(child);
            }
        }
    }
    public static Definition generateWsdlDefinition(HttpDefinition definition) throws WSDLException{
        if(StringUtils.isEmpty(definition.getMethodName())){
            throw new StdException("wsdlgenerator.err_method_name_is_not_allow_empty");
        }

        Guard.assertTrue(StringHelper.isValidVarName(definition.getMethodName()),definition.getMethodName()+"不是合法的变量名");
        String prefix = StringHelper.capitalize( definition.getMethodName());
        Param headers  = toParam(definition.getHeaders(),prefix,"Headers");
        Param params  = toParam(definition.getParams(),prefix,"Params");
        Param path  = toParam(definition.getPath(),prefix,"PathParams");
        Param body = null;
        if("json".equals(definition.getReqType())){
            body  = toParam(definition.getBody(),prefix,"Body");
        }else{ // form或者其他
            body  = formBodyToParam(definition.getBody(),prefix,"Body");
        }

        Param resHeaders  = toParam(definition.getRespHeaders(),prefix,RESULT_NAME+"Headers");
        if(resHeaders != null){
            resHeaders.setName("headers");
        }
        Param resBody  = toParam(definition.getRespBody(),prefix,RESULT_NAME+"Body");
        if(resBody != null){
            resBody.setName("body");
        }

        List input = new ArrayList();
        if(headers!=null){
            input.add(headers);
        }
        if(body!=null){
            input.add(body);
        }
        if(params!=null){
            input.add(params);
        }
        if(headers!=null){
            input.add(headers);
        }
        if(path!=null){

            input.add(path);
        }

        Param response = new Param();
        response.setName(prefix+RESULT_NAME);
        response.setParamType(ParamType.OBJECT);
        if(resHeaders != null){
            response.getChildren().add(resHeaders);
        }
        if(resBody != null){
            response.getChildren().add(resBody);
        }

        WsdlModelInfo wsdlModelInfo = new WsdlModelInfo();
        wsdlModelInfo.setEndpointUrl(definition.getWebServiceCallUrl());
        String pkgName = definition.getPkgName();
        if(StringUtils.isEmpty(pkgName)){
            pkgName = defaultPkgName;
        }

        String serviceName = definition.getServiceName();
        if(StringUtils.isEmpty(serviceName)){
            serviceName = definition.getMethodName()+"Service";
        }

        wsdlModelInfo.setTargetNamespace(getNamespaceByPkgName(pkgName));
        wsdlModelInfo.setServiceName(serviceName);
        List<ServiceMethodInfo> methods = new ArrayList<>();
        wsdlModelInfo.setMethods(methods);

        ServiceMethodInfo method = new ServiceMethodInfo(definition.getMethodName(),false);
        method.setInputParams(input);
        method.setOutParam(response);
        methods.add(method);
        WsdlGenerator  wsdlGenerator = new WsdlGenerator(wsdlModelInfo);
        Definition def = wsdlGenerator.buildWsdlDefinition();
        return def;
    }
    public static String generateWsdl(HttpDefinition httpDefinition) throws WSDLException {
        Definition definition = generateWsdlDefinition(httpDefinition);
        String wsdlContent =   WsdlUtils.wsdlToString(definition);
        return wsdlContent;
    }
    public static  Param  toParam(List<? extends JsonType> body, String prefix,String name){
        if(body == null || body.isEmpty()) return null;
        for (JsonType jsonType : body) {
            validateClassName(jsonType);
        }
        Param param = new Param();
        param.setName(StringHelper.decapitalize(name));
        param.setClassName(prefix+name);
        param.setParamType(ParamType.OBJECT);
        for (JsonType jsonType : body) {
            param.getChildren().add(toParam(jsonType));
        }
        return param;
    }
    public static Param formBodyToParam(List<JsonType> bodys, String prefix, String name){
        if(bodys == null || bodys.isEmpty()) return null;

        Param body = new Param();
        body.setName(StringHelper.decapitalize(name));
        body.setClassName(prefix+name);
        body.setParamType(ParamType.OBJECT);



        Param root = new Param();
        root.setName("root");
        root.setParamType(ParamType.OBJECT);
        for (JsonType jsonType : bodys) {
            root.getChildren().add(toParam(jsonType));
        }
        body.getChildren().add(root);


        return body;
    }
    public static String getNamespaceByPkgName(String pkgName){
        if(StringUtils.isEmpty(pkgName)) return pkgName;
        String[] split = StringUtils.split(pkgName, '.');
        List<String> results = new ArrayList<>();
        for (int i = split.length - 1; i >= 0 ; i--) {
            results.add(split[i]);
        }
        String namespace =  results.stream().collect(Collectors.joining("."));
        namespace = "http://"+namespace+"/";
        return namespace;
    }
    public static Param toParam(JsonType jsonType){
        Param param = new Param();
        param.setName(camelParamName(jsonType.getName()));
        param.setClassName(jsonType.getClassName());
        param.setRequired(jsonType.isRequired());

        if(jsonType instanceof ArrayJsonType){
            param.setParamType(ParamType.ARRAY);
            List<JsonType> children = ((ArrayJsonType) jsonType).getChildren();
            if(children.size() > 1){
                throw new StdException("wsdl.err_transform_cause_exist_array_type").param("name",jsonType.getName());
            }
            for (JsonType child : children) {
                param.getChildren().add(toParam(child));
            }
        }else if(jsonType instanceof ObjectJsonType){
            param.setParamType(ParamType.OBJECT);
            Set<String> names = new HashSet<>();
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                boolean add = names.add(child.getName());
                if(!add){
                    throw new StdException("wsdl.err_object_type_exist_same_name").param("name",child.getName());
                }
                param.getChildren().add(toParam(child));
            }
        }else{
            param.setParamType(ParamType.from(jsonType.getType()));
        }
        return param;
    }

    /**
     *  header、params、body里的参数名从-、_分割转换为驼峰了，需要存一下原始的字段名,原始名存放到rawName字段里
     * @param envelopType {
     *             "name": "Envelope",
     *             "type": "object",
     *             "children": [{
     *                     "name": "Header"
     *                 }, {
     *                     "name": "Body",
     *                     "children": [{
     *                             "name": "test",
     *                             "type": "object",
     *                             "children": [{
     *                                     "name": "headers",
     *                                     "required": true,
     *                                     "type": "object",
     *                                     "children": [{
     *                                             "name": "accept",
     *                                             "type": "string"
     *                                         }, {
     *                                             "name": "contentType",
     *                                             "type": "string"
     *                                         }
     *                                     ]
     *                                 }, {
     *                                     "name": "body",
     *                                     "required": true,
     *                                     "type": "object",
     *                                     "children": [{
     *                                             "name": "test1",
     *                                             "type": "string"
     *                                         }
     *                                     ]
     *                                 }, {
     *                                     "name": "params",
     *                                     "required": true,
     *                                     "type": "object",
     *                                     "children": [{
     *                                             "name": "test2",
     *                                             "type": "string"
     *                                         }
     *                                     ]
     *                                 }
     *                             ]
     *                         }
     *                     ]
     *                 }
     *             ]
     *         }
     * @param definition
     */
    public static void setReqRawName(JsonType envelopType,HttpDefinition definition){

        JsonType opType = JsonTypeUtils.getEnvelopOpType(envelopType);

        if(opType == null || !(opType instanceof ObjectJsonType)) return;
        setRawName((ObjectJsonType) JsonTypeUtils.get(opType,"headers"),definition.getHeaders());
        setRawName((ObjectJsonType) JsonTypeUtils.get(opType,"body"),definition.getBody());
        setRawName((ObjectJsonType) JsonTypeUtils.get(opType,"params"),definition.getParams());
        setRawName((ObjectJsonType) JsonTypeUtils.get(opType,"pathParams"),definition.getPath());
    }
    private static boolean isMatch(String name,String name2){
        if(name == null || name2 == null) return false;
        String name11 = camelParamName(name).toLowerCase();
        String name22 = camelParamName(name2).toLowerCase();
        return name11.equals(name22);
    }
    public   static void setRawName(List<? extends JsonType> rawTypes, List<? extends JsonType> originalTypes,boolean isArray){
        if(rawTypes == null) return;
        int index = 0;
        for (JsonType jsonType : rawTypes) {
            index++;
            if(isArray){
                setRawName(jsonType,originalTypes.get(index-1));
                continue;
            }
            for (JsonType type : originalTypes) {
                if(isMatch(jsonType.getName(),type.getName())){
                    setRawName(jsonType,type);
                }
            }
        }
    }


    /**
     *  http转webservice的时候，content-type字段名被转换为contentType了，需要记录一下下原始的字段名
     * @param jsonType
     * @param originalChildTypes
     */
    public  static void setRawName(ObjectJsonType jsonType, List<? extends JsonType> originalChildTypes){
        if(jsonType == null) return;
        setRawName(jsonType.getChildren(),originalChildTypes,false);
    }
    public static void setRawName(JsonType rawType, JsonType originalType){
        if(rawType == null || originalType == null) return;
         rawType.setRawName(originalType.getName());

        List<JsonType> children1 = null;
        List<JsonType> children2 = null;
        boolean isArray = false;
        if(rawType instanceof ObjectJsonType ){
            children1 = ((ObjectJsonType) rawType).getChildren();
            children2 = ((ObjectJsonType) originalType).getChildren();
        }else if(rawType instanceof ArrayJsonType ){
            children1 = ((ArrayJsonType) rawType).getChildren();
            children2 = ((ArrayJsonType) originalType).getChildren();
            isArray = true;
        }
       setRawName(children1,children2,isArray);
    }
    /**
     * {
     *             "name": "Envelope",
     *             "namespacePrefix": "soapenv",
     *             "attrs": {
     *                 "xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
     *                 "xmlns:test": "http://test/"
     *             },
     *             "type": "object",
     *             "children": [{
     *                     "name": "Header",
     *                     "namespacePrefix": "soapenv",
     *                     "type": "object",
     *                     "children": []
     *                 }, {
     *                     "name": "Body",
     *                     "namespacePrefix": "soapenv",
     *                     "type": "object",
     *                     "children": [{
     *                             "name": "testResponse",
     *                             "namespacePrefix": "test",
     *                             "type": "object",
     *                             "children": [{
     *                                     "name": "body",
     *                                     "type": "object",
     *                                     "children": [{
     *                                             "name": "root",
     *                                             "type": "string"
     *                                         }
     *                                     ]
     *                                 }
     *                             ]
     *                         }
     *                     ]
     *                 }
     *             ]
     *         }
     * @param envelopType
     * @param definition
     */
    public static void setRespRawName(JsonType envelopType,HttpDefinition definition){

        JsonType returnType =  JsonTypeUtils.getHttp2WsRespReturnType(envelopType);
        if(returnType == null || !(returnType instanceof ObjectJsonType)) return;
        setRawName((ObjectJsonType) JsonTypeUtils.get(returnType,"headers"),definition.getRespHeaders());
        setRawName((ObjectJsonType) JsonTypeUtils.get(returnType,"body"),definition.getRespBody());
    }
    /**
     * 参数名自动转换为java规范的驼峰形式。
     * 如_、-会被转换为驼峰形式
     * @param paramName
     * @return
     */
    public static String camelParamName(String paramName){

        return StringHelper.camelParamName(paramName);
    }

}
