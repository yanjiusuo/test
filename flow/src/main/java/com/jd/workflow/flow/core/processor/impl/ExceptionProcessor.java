package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.metadata.impl.CollectStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验入参是否与录入的一致
 */
public class ExceptionProcessor implements StepProcessor<CollectStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(ExceptionProcessor.class);

    CollectStepMetadata metadata;


    /**
     * 这里的metadata结构为：
     *  {
     *      output:{}
     *  }，
     *  output实际对应workflowInput的failOutput
     *
     * @param args 步骤参数，序列化为json以后的参数,初始化的时候才会被调用
     */
    @Override
    public void init(CollectStepMetadata args) {
        this.metadata = args;
    }

    @Override
    public String getTypes() {
        return "exception";
    }

    @Override
    public void process(Step currentStep) {
        StepExecException exception = currentStep.getContext().getException();
        logger.error("exception.err_process:", exception);
        HttpOutput output = new HttpOutput();
        String errorMsg = ErrorMessageFormatter.formatMsg(exception);
        try{
            if(metadata.getOutput() == null){
                output.setSuccess(false);
                output.setStatus(HttpStatus.SC_BAD_REQUEST);
                output.setBody(errorMsg);
            }else{
                ParametersUtils utils = new ParametersUtils();
                output.setStatus(HttpStatus.SC_OK);
                if(metadata.getOutput().getScript() != null){
                    Map<String,Object> args = utils.getMvelExecVars(currentStep.getContext());
                    args.put("workflow",currentStep.getContext());
                    args.put("output",output);
                    MvelUtils.eval("failOutput","script",metadata.getOutput().getScript(),args,output);
                }else{
                    ParamMappingContext paramMappingContext = new ParamMappingContext(currentStep.getContext());

                    Map<String,Object> map = MvelUtils.getJsonInputValue(metadata.getOutput().getHeaders(),paramMappingContext,output,"headers");
                    JsonType httpBody = JsonValidateUtils.getHttpBody(metadata.getOutput().getBody());
                    Object body  = MvelUtils.getJsonInputValue(httpBody,paramMappingContext,output,"body");
                    output.setHeaders(map);
                    output.setBody(body);
                }

            }
        }catch (Exception e){
            logger.error("step.error_process_exception_step",e);
            if(e instanceof StepExecException){
                output.setBody(ErrorMessageFormatter.formatMsg((StepExecException) e));
            }else{
                output.setBody(e.getMessage());
            }
            output.setStatus(HttpStatus.SC_BAD_REQUEST);

        }





        currentStep.setOutput(output);
        currentStep.getContext().setOutput(output);
    }
}
