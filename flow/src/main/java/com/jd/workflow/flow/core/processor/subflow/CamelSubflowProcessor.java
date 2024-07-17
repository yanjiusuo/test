package com.jd.workflow.flow.core.processor.subflow;

import com.jd.workflow.flow.core.camel.RouteStepBuilder;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.input.BaseInput;
import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.ISubflowProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.StepContextHelper;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CamelSubflowProcessor implements ISubflowProcessor {
    ProducerTemplate producerTemplate;

    public CamelSubflowProcessor(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void setProducerTemplate(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public Output execSubflow(String subflowId, Input input) {
        Exchange exchange = new DefaultExchange(producerTemplate.getCamelContext());
        StepContext stepContext = StepContextHelper.setInput(exchange, (WorkflowInput) input); // 设置输入，返回执行上下文
        stepContext.setDebugMode(false);
        producerTemplate.send("direct:"+ RouteStepBuilder.SUB_FLOW_PREFIX+subflowId,exchange);
        Output output = (Output) exchange.getMessage().getBody();
        output.attr("subflowContext",toMap(stepContext));
        return output;
    }
    public Map<String,Object> toMap(StepContext ctx){
        Map<String,Object> map = new LinkedHashMap<>();
        if(!ctx.getAttrs().isEmpty()){
            map.put("attrs",ctx.getAttrs());
        }
        map.put("input",ctx.getInput());


        map.put("exception", ErrorMessageFormatter.formatMsg(ctx.getException()));
        List stepsData =  new ArrayList();
        map.put("steps",stepsData);
        for (Map.Entry<String, Step> entry : ctx.getSteps().entrySet()) {
            Step step = entry.getValue();
            if(!StringUtils.isEmpty(step.getId()) && step.getId().startsWith("_")){// 内部步骤忽略掉
                continue;
            }
            if(step.isInvoked()){
                Map<String, Object> stepMetadata = step.toMap();
                stepsData.add(stepMetadata);
                Input input = step.getInput();
                Output output = step.getOutput();
                if(input !=null
                        && input instanceof BaseInput
                        && !((BaseInput)input).getVariables().isEmpty()){
                    stepMetadata.put("inputVariables", ((BaseInput)input).getVariables());
                }
                if(output != null
                        && output instanceof BaseOutput
                        && !((BaseOutput)output).getVariables().isEmpty()){
                    stepMetadata.put("outputVariables", ((BaseOutput)output).getVariables());
                }
            }
        }

        return map;
    }
}
