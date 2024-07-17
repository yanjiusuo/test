package com.jd.workflow.flow.loader;

import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.soap.common.util.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.xml.SpringModelJAXBContextFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;

@Slf4j
public class CamelRouteExecutor {


}
