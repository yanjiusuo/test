package com.jd.workflow.bean.ducc;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.bean.BeanStepDefinitionLoader;
import com.jd.workflow.flow.bean.BeanStepMetadata;
import com.jd.workflow.flow.bean.BeanStepProcessor;
import com.jd.workflow.flow.bean.BeanTemplateDefinition;
import com.jd.workflow.flow.bean.ducc.DuccConfig;
import com.jd.workflow.flow.bean.ducc.DuccFlowBean;
import com.jd.workflow.flow.bean.jimdb.JimdbConfig;
import com.jd.workflow.flow.core.bean.IBeanStepProcessor;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JimdbTests extends BaseTestCase {
    String jimUrl = "jim://2914179041246144323/110000253";
    /**
     *
     */
    @Test
    public void testDuccBean(){
        String key = "wjf";
        String value = "123431";
        Output setResult = invokeMethod("set", key, value);
        Output getResult = invokeMethod("get", key);
        System.out.println(JsonUtils.toJSONString(setResult));
        System.out.println(JsonUtils.toJSONString(getResult));
        assertEquals(getResult.getBody(),value);
    }
    private Output invokeMethod(String methodName,Object ...args){
        BeanStepProcessor processor = new BeanStepProcessor();
        BeanTemplateDefinition definition = BeanStepDefinitionLoader.getBeanDefinition("jimdb");

        log.info("bean_template_definition={}",JsonUtils.toJSONString(definition));

        MethodMetadata getMethodDefinition = null;
        for (MethodMetadata definitionMethod : definition.getMethods()) {
            if(definitionMethod.getMethodName().equals(methodName)){
                getMethodDefinition = definitionMethod;
            }

        }


        Map<String,Object> initValue = new HashMap<>();
        initValue.put("url",jimUrl);

        BeanStepMetadata setStepMetadata = BeanStepMetadata.from(getMethodDefinition);
        setStepMetadata.setBeanType("jimdb");
        setStepMetadata.setInitConfigClass(JimdbConfig.class.getName());
        setStepMetadata.setInitConfigValue(initValue);
        setStepMetadata.init();
        processor.init(setStepMetadata);
        for (int i = 0; i < args.length; i++) {
            getMethodDefinition.getInput().get(i).setValue(args[i]);
        }


        Step currentStep = new Step();
        WorkflowInput workflowInput = new WorkflowInput();

        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);
        currentStep.setContext(stepContext);
        processor.process(currentStep);
        final Output output = currentStep.getOutput();
        System.out.println(JsonUtils.toJSONString(output));
        return output;

    }
}
