package com.jd.workflow.flow.core.processor.impl;


import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.legacy.SoapVersion;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 将webservice转换为http步骤， 入参转换：soap xml -> req json -> resp json -> soap response
 {
 input:{
    url:'',
     schemaType:{
         "name": "Envelope",
         "namespacePrefix": "soapenv",
         "attrs": {
         "xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
         "xmlns:ser": "http://service.workflow.jd.com/"
     },
     "type": "object",
     "children": [{
         "name": "Body",
         "namespacePrefix": "soapenv",
         "type": "object",
         "children": [{
         "name": "test",
         "namespacePrefix": "ser",
         "type": "object",
         "children": [{}]
     }, {
         "name": "Header",
         "namespacePrefix": "soapenv",
         "attrs": {
         "  xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/"
         },
     "type": "object",
     "children": []
     }]
 }
 },
 output:{
 schemaType:{}
 }

 }
 */
public class Ws2HttpStepProcessor extends BaseHttpStep implements StepProcessor<Ws2HttpStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(Http2WsStepProcessor.class);


    Ws2HttpStepMetadata stepMetadata;
    @Override
    public void init(Ws2HttpStepMetadata metadata) {
        this.stepMetadata = metadata;
        String id = metadata.getId();
        if(id == null){
            id = "";
        }
        setTaskDefinition(stepMetadata.getTaskDef());
        setId(id);
    }

    @Override
    public String getTypes() {
        return "ws2http";
    }

    @Override
    public void process(Step currentStep) {
        StepContext context = currentStep.getContext();
        HttpInput httpInput = new HttpInput();
        HttpOutput output = new HttpOutput();
        currentStep.setOutput(output);
        currentStep.setInput(httpInput);
        try{
            Object o =  context.getInput().getBody();
            if(!(o instanceof String)){
                throw new StepExecException("input","无效的输入，输入类型必须为xml");
            }
            String reqBody = (String) o;
            Map<String,Object> json = (Map<String, Object>) SoapUtils.soapXmlToJson(reqBody, stepMetadata.getInput().getSchemaType());
            Map<String,Object> envelopBody = (Map<String, Object>) json.get("Body");

            buildHttpInput(httpInput, envelopBody,context,stepMetadata.getInput().getSchemaType());

            callHttp( httpInput,output);
            output.setContentType(ContentType.APPLICATION_XML.getMimeType());
            JsonType outputSchemaType = stepMetadata.getOutput().getSchemaType();


            JsonType returnType =   JsonTypeUtils.getHttp2WsRespReturnType(outputSchemaType); // 返回值类型
            if(returnType instanceof ObjectJsonType){ // 只有对象类型才可以继续设置header以及body，非对象类型说明body为空
                // 需要转换大小写以及-参数
                Map<String,Object> values = collectOutputValues(output,returnType);

                returnType.setValue(values);
            }else{ // simpleJsonType，不需要设置啊

            }


            List<XNode> xNodes = outputSchemaType.transformToXml();
            String result = xNodes.get(0).toXml();
            output.setBody(result);
        }catch (StepExecException e){
            logger.error("ws.err_process_ws",e);
            String result = SoapMessageBuilder.buildFault("soap:Server", e.getMessage(), SoapVersion.Soap11, SoapContext.DEFAULT);
            output.setStatus(HttpStatus.SC_BAD_REQUEST);
            output.setBody(result);
        }catch (Exception e){
            logger.error("ws.err_process_ws",e);
            String result = SoapMessageBuilder.buildFault("soap:Server", e.getMessage(), SoapVersion.Soap11, SoapContext.DEFAULT);
            output.setStatus(HttpStatus.SC_BAD_REQUEST);
            output.setBody(result);
        }

    }
    Map<String,Object> collectOutputValues(HttpOutput output,JsonType returnType){
        ObjectJsonType headerJsonType = (ObjectJsonType) JsonTypeUtils.get(returnType,"headers");
        JsonType bodyJsonType =  JsonTypeUtils.get(returnType,"body","root");

        Map<String,Object> values = new HashMap<>();
        Map<String,Object> root = new HashMap<>();
        values.put("headers",collectHeaders(output, headerJsonType));
        values.put("body",root);
        root.put("root", transformParamUseRawName(bodyJsonType,output.getBody(),false));
        return values;
    }



    private void buildHttpInput(HttpInput httpInput, Map<String, Object> envelopBody,StepContext stepContext,JsonType reqEnvelopType) {
        Map<String,Object> service = null;
        for (Map.Entry<String, Object> entry : envelopBody.entrySet()) {
            service = (Map<String, Object>) entry.getValue(); // 第一个报文是包裹
        }
        if(service == null){
            service = new HashMap<>();
        }
        JsonType opType = JsonTypeUtils.getEnvelopOpType(reqEnvelopType);

        Map<String,Object> params = (Map<String, Object>) service.get("params");
        Map<String,Object> headers = (Map<String, Object>) service.get("headers");
        Map<String,Object> body = (Map<String, Object>) service.get("body");
        Map<String,Object> pathParams = (Map<String, Object>) service.get("pathParams");
        if(body !=null){
            Object bodyObj = body.get("root");
            bodyObj = transformParamUseRawName(JsonTypeUtils.get(opType,"body","root"),bodyObj,true);
            httpInput.setBody(bodyObj);
        }



        httpInput.setParams((Map<String, Object>) transformParamUseRawName(JsonTypeUtils.get(opType,"params"),params,true));
        httpInput.setHeaders((Map<String, Object>) transformParamUseRawName(JsonTypeUtils.get(opType,"headers"),headers,true));
        Map<String,Object> actualPathParams = (Map<String, Object>) transformParamUseRawName(JsonTypeUtils.get(opType,"pathParams"),pathParams,true);
        httpInput.setMethod(stepMetadata.getHttpMethod());
        httpInput.setReqType(stepMetadata.getReqType());
        setCookie(httpInput,stepContext);
        String endpointUrl = buildFullUrl(stepMetadata.getEndpointUrl(), stepMetadata.getUrl());
        String inputUrl = StringHelper.replacePlaceholder(endpointUrl,actualPathParams);

        httpInput.setUrl(inputUrl);
    }

    /**
     *  java不支持-分割的字符，比如content-type被转换为contentType，
     *  调用实际接口的时候，需要转换回来
     * @param jsonType
     * @param data
     * @param name2RawName 是否将rawName转换为name,true的为name转为rawName，false的话为rawName转到name
     * @return
     */
    Object transformParamUseRawName(JsonType jsonType,Object data,boolean name2RawName){
        if(data == null) return data;
        if(jsonType instanceof ObjectJsonType){
            Map map = (Map) data;
            Map ret = new LinkedHashMap();
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                if(name2RawName){
                    Object value = map.get(child.getName());
                    String name = child.getRawNameDefaultName();
                    ret.put(name,transformParamUseRawName(child,value,name2RawName));
                }else{ // rawName2Name
                    Object value = map.get(child.getRawNameDefaultName());
                    String name = child.getName();
                    ret.put(name,transformParamUseRawName(child,value,name2RawName));
                }

            }
            return ret;
        }else if(jsonType instanceof ArrayJsonType){
            ArrayJsonType arrayJsonType = (ArrayJsonType) jsonType;
            List ret = new ArrayList();

            List list = (List) data;
            for (Object o : list) {
                ret.add(transformParamUseRawName(arrayJsonType.getChildren().get(0),o,name2RawName));
            }
            return ret;
        }
        return data;
    }
    private Map<String,Object> collectHeaders(HttpOutput output,ObjectJsonType headerJsonType) {
        if(headerJsonType == null){
            return new HashMap<>();
        }
        Map<String,Object> ret = new LinkedHashMap<>();
        Map<String, Object> headers = new LinkedCaseInsensitiveMap<>();
        headers.putAll(output.getHeaders());
        for (JsonType child : headerJsonType.getChildren()) {
            String name = child.getRawNameDefaultName();

            ret.put(child.getName(),headers.get(name));
        }
        return ret;
    }



    public static void main(String[] args) {
        Class actualTypeArgument = (Class)((ParameterizedType) Ws2HttpStepProcessor.class.getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(actualTypeArgument);
    }

}
