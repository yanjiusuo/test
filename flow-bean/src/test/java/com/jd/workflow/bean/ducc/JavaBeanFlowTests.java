package com.jd.workflow.bean.ducc;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.bean.ducc.service.DemoService;
import com.jd.workflow.bean.ducc.service.DemoUtils;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JavaBeanFlowTests extends BaseTestCase {
    public Output debugFlow(String config, WorkflowInput input, List<String> utilsClass, Map<String,Object> bean){
        WorkflowDefinition def = null;

            def = WorkflowParser.parse(config);


        CamelRouteLoader routeLoader = new CamelRouteLoader();
        try {
            routeLoader.initFlowEnv(utilsClass,bean);
            CamelContext camelContext = routeLoader.buildCamelContext(def);
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {

                Exchange exchange = new DefaultExchange(camelContext);
                StepContext stepContext  =  StepContextHelper.setInput(exchange,input); // 设置输入，返回执行上下文
                stepContext.setDebugMode(true);
                template.send("direct:start", exchange);// 执行代码

                Output output = (Output) exchange.getMessage().getBody();
                    log.info("camel_exec_result={}", JsonUtils.toJSONString(output));
                    return output;
            }finally {
                camelContext.stop();

            }

        } catch (Exception e) {
            log.error("debug.err_build_route_context",e);
            throw new BizException("调试失败",e);
        }
    }
    @Test
    public void testDemoService(){
        String def = getResourceContent("classpath:bean/service-bean.json");
        Map<String,Object> beanRefs = new HashMap<>();
        beanRefs.put("demoService",new DemoService());
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("c",123);
        workflowInput.setBody(body);
        Output output = debugFlow(def,workflowInput,null,beanRefs);
        assertEquals("123",output.getBody());
    }
    @Test
    public void testDemoUtils(){
        String def = getResourceContent("classpath:bean/utils-bean.json");
        Map<String,Object> beanRefs = new HashMap<>();
        beanRefs.put("demoService",new DemoService());
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("c",123);
        workflowInput.setBody(body);
        Output output = debugFlow(def,workflowInput,null,beanRefs);
        assertEquals("123",output.getBody());
    }

    @Test
    public void testBeanScript(){
        String def = getResourceContent("classpath:bean/bean-script.json");
        Map<String,Object> beanRefs = new HashMap<>();
        beanRefs.put("demoService",new DemoService());
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("c",123);
        workflowInput.setBody(body);
        Output output = debugFlow(def,workflowInput,null,beanRefs);
        assertEquals("123",output.getBody());
    }
    @Test
    public void testUtilsScript(){
        String def = getResourceContent("classpath:bean/utils-script.json");
        Map<String,Object> beanRefs = new HashMap<>();
        beanRefs.put("demoService",new DemoService());
        List<String> utils = new ArrayList<>();
        utils.add(DemoUtils.class.getName());
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("c",123);
        workflowInput.setBody(body);
        Output output = debugFlow(def,workflowInput,utils,beanRefs);
        assertEquals("123",output.getBody());
    }
}
