package com.jd.workflow.flow.bean;

import com.jd.workflow.flow.core.bean.IBeanStepProcessor;
import com.jd.workflow.flow.core.context.FlowExecContext;
import com.jd.workflow.flow.core.context.FlowContextAware;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.WorkflowParserException;
import com.jd.workflow.flow.core.input.DefaultInput;
import com.jd.workflow.flow.core.output.DefaultOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeanStepProcessor implements StepProcessor<BeanStepMetadata>, FlowContextAware {
    Object bean;
    Method callMethod;
    static ParametersUtils utils = new ParametersUtils();
    IBeanStepProcessor beanProcessor ;
     BeanStepMetadata metadata;
     FlowExecContext flowContext;
    @Override
    public void setFlowContext(FlowExecContext flowContext) {
        this.flowContext = flowContext;
    }
    @Override
    public void init(BeanStepMetadata metadata) {
        Class beanClass;
        this.metadata = metadata;
        if(StringHelper.isNotBlank(metadata.getBeanName())){
            bean = flowContext.getBeanRegistry().get(metadata.getBeanName());
            if ((bean == null)) {
                throw new WorkflowParserException("flow.err_miss_java_bean").param("beanName",metadata.getBeanName());
            }
            beanClass = bean.getClass();
        }else if(metadata.getServiceType() != null && metadata.getServiceType() == 1){
            bean = null;
            try {
                beanClass = Class.forName(metadata.getBeanType());
            } catch (ClassNotFoundException e) {
                throw new WorkflowParserException("flow.err_invalid_java_bean").param("beanType",metadata.getBeanType());
            }
        }else{
            beanProcessor = BeanStepDefinitionLoader.getBeanProcessor(metadata.getBeanType());

            Object initArgs = null;
            try {
                Object value = JsonUtils.cast(metadata.getInitConfigValue(), Class.forName(metadata.getInitConfigClass()));
                initArgs = value;
            } catch (ClassNotFoundException e) {
                throw StdException.adapt(e);
            }
            bean = beanProcessor.getBeanFactory().init(initArgs);
            beanClass = bean.getClass();
        }

        for (Method method : beanClass.getMethods()) {
            if(
                    method.getName().equals(metadata.getMethodName()) &&
                    method.getParameterCount() == metadata.getInput().size()){
               callMethod = method;

            }
        }
        if(callMethod == null){
            throw new StdException("bean.err_no_miss_such_method").param("beanType",metadata.getBeanType()).param("method",metadata.getMethodName());
        }

    }

    @Override
    public String getTypes() {
        return "bean,javabean";
    }

    @Override
    public void process(Step currentStep) {
        DefaultInput defaultInput = new DefaultInput();
        DefaultOutput defaultOutput = new DefaultOutput();
        currentStep.setInput(defaultInput);
        currentStep.setOutput(defaultOutput);

        List inputValues = new ArrayList(metadata.getInput().size());
        if(metadata.getScript() != null){
            Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
            vars.put("input",defaultInput);
            vars.put("output",defaultOutput);
            MvelUtils.eval(metadata.getId(),"script", metadata.getScript(),vars,defaultInput);
            if(!(defaultInput.getBody() instanceof List)
                    || (((List)defaultInput.getBody()).size() != metadata.getInput().size())
            ){
                throw new StepExecException(metadata.getId(),"jsf.err_jsf_input_must_be_array_and_args_count_must_match")
                        .param("count",metadata.getInput().size());
            }
             inputValues = (List) defaultInput.getBody();
        }else{
            ParamMappingContext paramMappingContext = new ParamMappingContext(currentStep.getContext(),null);
            Map<String, Object> mappingValue = utils.getJsonInputValue(metadata.getInput(), paramMappingContext);
            for (JsonType jsonType : metadata.getInput()) {
                inputValues.add(mappingValue.get(jsonType.getName()));
            }
        }

        Object[] methodArgs = new Object[metadata.getInput().size()];
        int i = 0;
        for (JsonType jsonType : metadata.getInput()) {
            Object value = inputValues.get(i);
            try {
                Object convertValue = JsonUtils.cast(value, Class.forName(jsonType.getClassName()));
                methodArgs[i++] = convertValue;
            } catch (ClassNotFoundException e) {
                throw StdException.adapt(e);
            }
        }

        try {
             defaultInput.setBody(methodArgs);
             Object result = callMethod.invoke(bean, methodArgs);
             defaultOutput.setBody(result);
        } catch (Exception e) {
            throw new StdException("bean.err_invoke_bean_method",e)
                    .param("message",e.getMessage())
                    .param("type",this.metadata.getBeanType());
        }
    }

    @Override
    public void stop() {
        beanProcessor.getBeanFactory().destroy(bean);
    }


}
