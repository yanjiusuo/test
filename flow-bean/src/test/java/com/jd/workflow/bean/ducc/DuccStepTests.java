package com.jd.workflow.bean.ducc;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.bean.BeanStepDefinitionLoader;
import com.jd.workflow.flow.bean.BeanStepMetadata;
import com.jd.workflow.flow.bean.BeanStepProcessor;
import com.jd.workflow.flow.bean.BeanTemplateDefinition;
import com.jd.workflow.flow.bean.ducc.DuccConfig;
import com.jd.workflow.flow.bean.ducc.DuccFlowBean;
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
import org.apache.commons.beanutils.MethodUtils;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DuccStepTests extends BaseTestCase {
    /**
     *
     */
    @Test
    public void testDuccBean(){
        BeanStepProcessor processor = new BeanStepProcessor();
        IBeanStepProcessor duccProcessor = BeanStepDefinitionLoader.getBeanProcessor("ducc");
        BeanTemplateDefinition definition = BeanStepDefinitionLoader.getBeanDefinition("ducc");

        log.info("bean_template_definition={}",JsonUtils.toJSONString(definition));

        MethodMetadata getMethodDefinition = null;
        for (MethodMetadata definitionMethod : definition.getMethods()) {
            if(definitionMethod.getMethodName().equals("getProperty")){
                getMethodDefinition = definitionMethod;
            }
        }
         String uri = "ucc://jd-components:c44cb57c-c69e-41d9-a8cf-45633343b620@test.ducc.jd.local/v1/namespace/flow_assemble/config/app.integration-paas/profiles/dev?longPolling=60000&necessary=false" ;


        Map<String,Object> initValue = new HashMap<>();
        initValue.put("url",uri);

        BeanStepMetadata beanStepMetadata = BeanStepMetadata.from(getMethodDefinition);

        beanStepMetadata.setInitConfigClass(DuccConfig.class.getName());
        beanStepMetadata.setInitConfigValue(initValue);
        beanStepMetadata.init();
        processor.init(beanStepMetadata);

        getMethodDefinition.getInput().get(0).setValue("history");

        Step currentStep = new Step();
        WorkflowInput workflowInput = new WorkflowInput();

        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);
        currentStep.setContext(stepContext);
        processor.process(currentStep);
        final Output output = currentStep.getOutput();
        System.out.println(JsonUtils.toJSONString(output));

    }

    @Test
    public void testEmptyMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getProperty = DuccFlowBean.class.getMethod("getProperty", String.class);
        DuccConfig duccConfig = new DuccConfig();
        duccConfig.setUrl("ucc://jd-components:c44cb57c-c69e-41d9-a8cf-45633343b620@test.ducc.jd.local/v1/namespace/flow_assemble/config/app.integration-paas/profiles/dev?longPolling=60000&necessary=false");
        final Object result = getProperty.invoke(new DuccFlowBean(duccConfig), new Object[]{null});
        System.out.println(result);
    }
    @Test
    public void testNonEmptyMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getProperty = DuccFlowBean.class.getMethod("getPropertyTest", Object.class);
        DuccConfig duccConfig = new DuccConfig();
        duccConfig.setUrl("ucc://jd-components:c44cb57c-c69e-41d9-a8cf-45633343b620@test.ducc.jd.local/v1/namespace/flow_assemble/config/app.integration-paas/profiles/dev?longPolling=60000&necessary=false");

        DuccFlowBean flowBean = new DuccFlowBean(duccConfig);


        final Object result = MethodUtils.invokeExactMethod(flowBean, "getProperty", new Object[]{null});
        System.out.println(result);
    }

    @Test
    public void testDuccGetProperty(){

        final HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(new WorkflowInput(), "classpath:ducc/ducc-get-property.json");
        assertNotNull(output.getBody());
        System.out.println(JsonUtils.toJSONString(output));
    }

    @Test
    public void testDuccScript(){

        final HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(new WorkflowInput(), "classpath:ducc/ducc-script.json");
        assertNotNull(output.getBody());
        System.out.println(JsonUtils.toJSONString(output));
    }
    @Test
    public void testDuccFallback(){

        final HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(new WorkflowInput(), "classpath:ducc/ducc-fallback.json");
        assertNotNull(output.getBody());
        System.out.println(JsonUtils.toJSONString(output));
    }

    @Test
    public void testMultiSameConfigDuccStep(){

        final HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(new WorkflowInput(), "classpath:ducc/ducc-multi-same-config-step.json");
        assertNotNull(output.getBody());
        System.out.println(JsonUtils.toJSONString(output));
    }
    @Test
    public void testMultiUniqueConfigDuccStep(){

        final HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(new WorkflowInput(), "classpath:ducc/ducc-multi-unique-config-step.json");
        assertNotNull(output.getBody());
        System.out.println(JsonUtils.toJSONString(output));
    }
}
