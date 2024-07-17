package com.jd.workflow.flow.core.processor.impl;


import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.WebServiceError;

import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStepMetadata;

import com.jd.workflow.flow.core.output.WebServiceOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;

import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;

import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.soap.common.xml.XNode;

import com.jd.workflow.soap.legacy.SoapVersion;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 通过http的方式去调用webservice服务参数转换方式为：json->req xml -> resp xml -> json
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
public class Http2WsStepProcessor extends BaseHttpStep implements StepProcessor<WebServiceStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(Http2WsStepProcessor.class);
    Map<String,Object> envelopeInputTemplate = new HashMap<>();


    WebServiceStepMetadata metadata;

    @Override
    public void init(WebServiceStepMetadata metadata) {
        this.metadata = metadata;
        setId(metadata.getId());
        setTaskDefinition(metadata.getTaskDef());
        envelopeInputTemplate = (Map<String, Object>) metadata.getInput().getSchemaType().toExprValue();
    }

    @Override
    public String getTypes() {
        return "http2ws";
    }

    /**
     * message是否包装了一次呢
     * @return
     */
    boolean isWrappedMessage(){
       return SoapUtils.isWrappedMessage(metadata.getInput().getSchemaType(),metadata.getOpName());
    }
    @Override
    public void process(Step currentStep) {
        WebServiceOutput output =  new WebServiceOutput();;
        currentStep.setOutput(output);
        try{
            HttpInput input = new HttpInput();
            Map<String,Object> envelope = buildInputValue(currentStep.getContext(),input);


            List<XNode> nodes = metadata.getInput().getSchemaType().transformToXml(envelope);
            String soapInput = nodes.get(0).toXml();

            String url = buildFullUrl(metadata.getEndpointUrl(),metadata.getUrl());

            logger.info("ws.req_info:url={},input={}",url,soapInput);


            input.setMethod("POST");
            input.setUrl(url);
            input.setReqType(ReqType.xml);
            input.setBody(soapInput);
            input.setContentType("text/xml");
            //input.setBody(envelope.get("Body"));
            if(input.getHeaders() == null){
                input.setHeaders(new HashMap<>());
            }
            input.getHeaders().put("SOAPAction", SoapVersion.Soap11.getSoapActionHeader(metadata.getSoapAction()));

            currentStep.setInput(input);

            currentStep.setOutput(output);
            callHttp( input,output);

            if(output.getException() != null) {
                StepExecException exception = (StepExecException) output.getException();
                String response = (String) output.getBody();
                if(!StringUtils.isEmpty(response)){
                    SOAPMessage soapMessage = SoapUtils.formatSoapString(response);
                    SOAPFault fault = soapMessage.getSOAPPart().getEnvelope().getBody().getFault();
                    WebServiceError error = initError(output, fault);
                    exception.getParams().remove("body");
                    exception.getParams().putAll(error.toMap());
                }

                throw output.getException();
            }else{
                String response = (String) output.getBody();
                if(StringUtils.isEmpty(response)){
                    throw new StepExecException(getId(),"webservice.err_response_is_empty");
                }
                SOAPMessage soapMessage = SoapUtils.formatSoapString(response);
                SOAPFault fault = soapMessage.getSOAPPart().getEnvelope().getBody().getFault();
                if(fault != null){ // 出错了
                    WebServiceError wsError = initError(output, fault);
                    StepExecException exception = new StepExecException(metadata.getId(),JsonUtils.toJSONString(wsError));
                    exception.getParams().putAll(wsError.toMap());
                    output.setException(exception);
                    throw exception;
                }
                Map<String,Object> result = (Map<String, Object>) SoapUtils.soapXmlToJson(soapMessage, metadata.getOutput().getSchemaType());
                Map<String,Object> body = (Map<String, Object>) result.get("Body");

                Map<String,Object> ret = new HashMap<>();
                if(isWrappedMessage()){
                    for (Map.Entry<String, Object> entry : body.entrySet()) {
                        ret = (Map<String, Object>) entry.getValue();
                    }
                }else{
                    ret = body;
                }

                output.setBody(ret);
                output.setHeaders((Map<String, Object>) result.get("Header"));

            }
        }catch (StepExecException e){

            throw e;
        }catch (ToXmlTransformException e){
            StepExecException exception = new StepExecException(getId(), e.getMsg(), e);
            exception.getParams().putAll(e.getParams());
            throw exception;
        }catch (Exception e){
            logger.error("http2ws.err_call:id={}",getId(),e);
            throw new StepExecException(getId(),"step.err_exec_step",e).param("message",e.getMessage());
        }


        /*JSONObject result = SoapUtils.parseSoapResponse(response);
        currentStep.setOutput(new DefaultOutput(result.toJavaObject(Map.class)));*/
    }
    private WebServiceError initError(WebServiceOutput output,SOAPFault fault) throws SOAPException {

        if(fault != null){ // 出错了
            WebServiceError wsError = SoapUtils.soapErrorToWsError(fault);
            output.setError(wsError);
            output.setStatus(HttpStatus.SC_BAD_REQUEST);
            output.setBody(wsError);
            output.setSuccess(false);
            return wsError;
        }
        return null;
    }
    Map<String,Object> buildInputValue(StepContext ctx,HttpInput input){

        Map<String,Object> envelope = MvelUtils.getTaskInput(envelopeInputTemplate, new ParamMappingContext(ctx),input,"input");
        Map<String,Object> castValue = (Map<String, Object>) metadata.getInput().getSchemaType().castValue(envelope);
        return castValue;
    }


   
}
