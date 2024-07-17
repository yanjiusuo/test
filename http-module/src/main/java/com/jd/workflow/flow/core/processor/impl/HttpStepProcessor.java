package com.jd.workflow.flow.core.processor.impl;


import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepValidateException;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.metadata.impl.HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HttpStepProcessor extends BaseHttpStep implements StepProcessor<HttpStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(HttpStepProcessor.class);



    HttpStepMetadata metadata;
    public HttpStepProcessor(){

    }



    private void buildInput(HttpInput httpInput,StepContext context){
        ParametersUtils utils = new ParametersUtils();
        if(metadata.getInput().getPreProcess() != null){
            Map<String, Object> vars = utils.getMvelExecVars(context);
            vars.put("input",httpInput);
            MvelUtils.eval(metadata.getId(),"preProcess", metadata.getInput().getPreProcess(),vars,httpInput);
        }



        HttpStepMetadata.Input input = metadata.getInput();

        httpInput.setMethod(input.getMethod());
        httpInput.setReqType(ReqType.valueOf(input.getReqType()));
        httpInput.setUrl(input.getUrl());
        String url = null;

        if(input.getScript() != null){
            Map<String, Object> vars = utils.getMvelExecVars(context);
            vars.put("input",httpInput);
            MvelUtils.eval(metadata.getId(),"script", input.getScript(),vars,httpInput);
            url = httpInput.getUrl();
        }else{
            Map<String,Object> extArgs = new HashMap<>();
            extArgs.put("input",httpInput.attrsMap());
            ParamMappingContext paramMappingContext = new ParamMappingContext(context,extArgs);

            url = httpInput.getUrl();
            if(!ObjectHelper.isEmpty(input.getPath())){
                Map<String, Object> pathParams = MvelUtils.getJsonInputValue(input.getPath(), paramMappingContext,httpInput,"path");
                url = StringHelper.replacePlaceholder(url,pathParams);
            }


            JsonType body = null;
            if(ReqType.json.name().equals(input.getReqType())){
                if(input.getBody()!=null && !input.getBody().isEmpty()){
                    body = input.getBody().get(0);

                    httpInput.setBody(MvelUtils.getJsonInputValue(body,paramMappingContext,httpInput,"input.body"));
                }
            }else if(ReqType.form.name().equals(input.getReqType())){
                if(input.getBody() != null
                        && !input.getBody().isEmpty()
                ){

                    httpInput.setBody(MvelUtils.getJsonInputValue(input.getBody(),paramMappingContext,httpInput,"input.body"));
                }
            }

 

            httpInput.setHeaders(MvelUtils.getJsonInputValue(input.getHeaders(),paramMappingContext,httpInput,"input.headers"));
            httpInput.setParams(MvelUtils.getJsonInputValue(input.getParams(),paramMappingContext,httpInput,"input.params"));

        }

        setCookie(httpInput,context);


        String callUrl = buildFullUrl(metadata.getEndpointUrl(),url);
        httpInput.setUrl(callUrl);
    }

    @Override
    public void init(HttpStepMetadata metadata) {
        this.metadata = metadata;
        setTaskDefinition(metadata.getTaskDef());
        setId(metadata.getId());
    }

    @Override
    public String getTypes() {
        return "http";
    }

    @Override
    public void process(Step currentStep) {

        try{
            StepContext context = currentStep.getContext();
            HttpInput input = new HttpInput();

            buildInput(input,context);
            currentStep.setInput(input);

            HttpOutput output = new HttpOutput();
            currentStep.setOutput(output);
            callHttp(input,output);
            if(output.getBody() != null && (output.getBody() instanceof Collection
                    || output.getBody() instanceof Map )){
                output.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            }

        }catch (StepExecException e){
            throw e;
        }catch (Exception e){
            logger.error("http.err_call:id={}",getId(),e);
            throw new StepExecException(metadata.getId(),"httpstep.err_unkown_exception",e).param("message",e.getMessage());
        }

    }

}
