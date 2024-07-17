package com.jd.workflow.flow;

import com.jd.workflow.FlowBaseTestCase;
import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.ListAggregationStrategy;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.processor.HttpFlowTests;
import com.jd.workflow.service.FullTypedWebService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.xml.SpringModelJAXBContextFactory;
import org.apache.camel.support.DefaultExchange;
import org.apache.cxf.endpoint.Server;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CamelStepRouteTests extends HttpBaseTestCase {

     @Test public void testAgg() throws Exception {
        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();

        DefaultCamelContext camelContext = new DefaultCamelContext();

        bindBeans(camelContext.getRegistry());


        StepContext stepContext = new StepContext();
        stepContext.setInput(new WorkflowInput());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());
        camelContext.addRouteDefinitions(camelRouteLoader.loadRoutesFromPath("camel/agg-step-bean.xml"));
        //camelContext.build();
        camelContext.start();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println(result);

    }
    protected static void bindBeans(Registry registry) throws Exception {
        registry.bind("ListAggregationStrategy",new ListAggregationStrategy());
        registry.bind("test", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getOut().setBody("123321321");
            }
        });
    }


    private DefaultCamelContext startRoute(String routePath) throws Exception {
        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());

        camelContext.addRouteDefinitions(camelRouteLoader.loadRoutesFromPath(routePath));
        //camelContext.build();
        camelContext.start();
        return camelContext;
    }
     @Test public void testExceptionStep() throws Exception {

        DefaultCamelContext camelContext = startRoute("camel/camel-exception-step-bean.xml");
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);


        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);


        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println(result);

    }
    public RouteDefinition loadRoute(Node node) throws IOException, JAXBException {
        Binder<Node> binder;


        JAXBContext jaxbContext = new SpringModelJAXBContextFactory().newJAXBContext();
        binder = jaxbContext.createBinder();
        //injectNamespaces(document.getDocumentElement(),binder,Namespaces.DEFAULT_NAMESPACE);
        JAXBElement unmarshal = binder.unmarshal(node, RouteDefinition.class);
        RouteDefinition definition = (RouteDefinition) unmarshal.getValue();
        return definition;
    }
    @Test
    public void testMultiRoutes() throws Exception {


        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());


        String routePath ="camel/camel-multi-route.xml";
        String content = getResourceContent("classpath:"+routePath);
        Document document = XmlUtils.parseXml(content);
        for (int i = 0; i < document.getDocumentElement().getChildNodes().getLength(); i++) {
            Node item = document.getDocumentElement().getChildNodes().item(i);
            if(item.getNodeName().equals("route")){
                RouteDefinition routeDef = loadRoute(item);
                camelContext.addRouteDefinitions(Collections.singletonList(routeDef));

            }
        }

        //camelContext.build();
        camelContext.start();


        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);


        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);


        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println("》》" + JsonUtils.toJSONString(result));

    }

     @Test public void testTransform() throws Exception {

        DefaultCamelContext camelContext = startRoute("camel/transform-demo.xml");
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);


        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);


        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println("》》" + JsonUtils.toJSONString(result));

    }

     @Test public void testTransformXml() throws Exception {

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<route xmlns=\"http://camel.apache.org/schema/spring\">\n" +
                "    <from uri=\"direct:start\"/>\n" +
                "    <bean beanType=\"com.jd.workflow.flow.core.camel.CamelStepBean\">\n" +
                "        <description>\n" +
                "            <![CDATA[{\"id\":\"step2\",\"type\":\"transform\",\n" +
                "                \"output\":{\n" +
                "                   \"body\":[{\n" +
                "                    \"name\":\"root\",\n" +
                "                    \"type\":\"string\",\n" +
                "                    \"value\":\"aa-123\"\n" +
                "                   }]}\n" +
                "                 }]]>\n" +
                "        </description>\n" +
                "    </bean>\n" +
                "</route>\n";
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());

        ExtendedCamelContext ecc = camelContext.adapt(ExtendedCamelContext.class);
        JAXBContext jaxbContext = (JAXBContext) ecc.getModelJAXBContextFactory().newJAXBContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RouteDefinition definition = (RouteDefinition) unmarshaller.unmarshal(is);
        camelContext.addRouteDefinition(definition);

        camelContext.start();

        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);


        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println("fromXml 》》》" + JsonUtils.toJSONString(result));

    }




    @Test public void testWorkflowInputValidate(){

        WorkflowInput workflowInput = new WorkflowInput();

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("cityId",-1);


        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/workflow-input-validate.json");
        assertEquals("cityId无效",result.getBody());
    }
    @Test
    public void testMockStep(){
        WorkflowInput workflowInput = new WorkflowInput();

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/mock/mock-step-definition.json");
        assertEquals("{\"code\":0,\"data\":1,\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
    }

    @Test public void testCamelLifecycle() throws Exception {
        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());
        camelContext.getRegistry().bind("lifeBean",new LifeCycleBean());


        camelContext.addRouteDefinitions(camelRouteLoader.loadRoutesFromPath("camel/camel-lifecycle-demo.xml"));
        //camelContext.build();
        camelContext.start();



        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("id", 1);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        camelContext.stop();
        Object result = exchange.getMessage().getBody();
        System.out.println(result);

    }
    static class LifeCycleBean implements Service{
         public void start(){
             System.out.println("start...");
         }

        @Override
        public void stop() {
            System.out.println("destroy...");
        }

        public void execute(){
             System.out.println("execute...");
         }

    }
}
