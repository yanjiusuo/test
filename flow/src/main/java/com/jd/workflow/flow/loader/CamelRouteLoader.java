package com.jd.workflow.flow.loader;

import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.subflow.CamelSubflowProcessor;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.xml.SpringModelJAXBContextFactory;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CamelRouteLoader {
    public List<RouteDefinition> loadRoutesFromPath(String path) throws IOException, JAXBException {
        Binder<Node> binder;
        Resource resource = new ClassPathResource(path);
        String config = IOUtils.toString(resource.getInputStream(), "utf-8");
        return loadRoutes(config);
    }

    public RouteDefinition loadRoute(String content) throws IOException, JAXBException {
        return loadRoutes(content).get(0);
    }
    public List<RouteDefinition> loadRoutes(String content) throws IOException, JAXBException {
        Binder<Node> binder;

        Document document = XmlUtils.parseXml(content);
        List<RouteDefinition> result = new ArrayList<>();
        if("routes".equals(document.getDocumentElement().getLocalName())){
            for (int i = 0; i < document.getDocumentElement().getChildNodes().getLength(); i++) {
                Node item = document.getDocumentElement().getChildNodes().item(i);
                if("route".equals(item.getNodeName())){
                    RouteDefinition routeDef = loadRoute(item);
                    result.add(routeDef);
                }
            }
        }else{
            result.add(loadRoute(document.getDocumentElement()));
        }

        return result;
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
    public  DefaultCamelContext buildCamelContext(WorkflowDefinition def) throws Exception {
        return buildCamelContext(def,null);
    }
    public  DefaultCamelContext buildCamelContext(WorkflowDefinition def,ApplicationContext context) throws Exception {
        String defXml  = RouteBuilder.buildRoute(def);
        return buildCamelContext(defXml,context);
    }
    public  DefaultCamelContext buildCamelContext(String defXml)throws Exception {
        return buildCamelContext(defXml,null);
    }
    public  DefaultCamelContext buildCamelContext(String defXml,ApplicationContext context) throws Exception {

        log.info("camel.build_camel_xml:{}",defXml);
        List<RouteDefinition> definitions =  this.loadRoutes(defXml);
        DefaultCamelContext camelContext = new DefaultCamelContext();
        if(context != null){
            camelContext = new SpringCamelContext(context);
        }
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());
        camelContext.addRouteDefinitions(definitions);
        return camelContext;
    }
    public static void initFlowEnv(List<String> pkgs, Map<String,Object> beanRefs){
        CustomMvelExpression.setUtilsClass(pkgs);
        CustomMvelExpression.setGlobalVariables(beanRefs);
    }
    public  DefaultCamelContext buildCamelContext(WorkflowDefinition def, List<String> pkgs, Map<String,Object> beanRefs, ApplicationContext applicationContext) throws Exception {
        String defXml  = RouteBuilder.buildRoute(def);
        log.info("camel.build_camel_xml:{}",defXml);
        RouteDefinition definition =  this.loadRoute(defXml);
        DefaultCamelContext camelContext = new DefaultCamelContext();
        if(applicationContext != null){
            camelContext = new SpringCamelContext(applicationContext);
        }
       /* if(beanRefs != null){
            for (Map.Entry<String, Object> entry : beanRefs.entrySet()) {
                camelContext.getRegistry().bind(entry.getKey(),entry.getValue());
            }
        }*/
        CustomLanguageResolver languageResolver = new CustomLanguageResolver();
        /*languageResolver.setUtilsClass(pkgs);
        languageResolver.setBeanRefs(beanRefs);*/
        camelContext.setLanguageResolver(languageResolver);
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());
        camelContext.addRouteDefinition(definition);
        return camelContext;
    }

    public StepContext debugFlow(Map<String,Object> definition, WorkflowInputDefinition inputDef, ApplicationContext context) {
        WorkflowDefinition def = WorkflowParser.parse(definition,null);

        CamelRouteLoader routeLoader = new CamelRouteLoader();
        try {
            CamelContext camelContext = routeLoader.buildCamelContext(def,context);

            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                WorkflowInput input = inputDef.toWorkflowInput();


                Exchange exchange = new DefaultExchange(camelContext);
                StepContext stepContext = StepContextHelper.setInput(exchange, input); // 设置输入，返回执行上下文
                stepContext.setSubflowProcessor(new CamelSubflowProcessor(template));
                stepContext.setDebugMode(true);

                template.send("direct:start", exchange);// 执行代码

                Output output = (Output) exchange.getMessage().getBody();
                if(stepContext.getException() != null){
                    log.error("flow.err_debug_flow",stepContext.getException());
                }

                return stepContext;
            } finally {
                camelContext.stop();

            }

        } catch (Exception e) {
            log.error("debug.err_build_route_context", e);
            throw new BizException("调试失败", e);
        }
    }
}
