package com.jd.workflow;

import com.jd.workflow.flow.AllRoutesTests;
import com.jd.workflow.flow.CamelStepTransfomTests;
import com.jd.workflow.flow.HttpRouteTests;
import com.jd.workflow.flow.MulticastRouteTests;
import com.jd.workflow.flow.parser.WorkflowParserTests;
import com.jd.workflow.processor.HttpProcessorTests;
import com.jd.workflow.processor.TransformProcessorTests;
import com.jd.workflow.processor.WebServiceProcessorTests;
import com.jd.workflow.processor.Ws2HttpProcessorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        HttpProcessorTests.class,
                WebServiceProcessorTests.class,
        Ws2HttpProcessorTests.class,

        TransformProcessorTests.class,
        WorkflowParserTests.class,
        AllRoutesTests.class,
        CamelStepTransfomTests.class,
        HttpRouteTests.class,
        MulticastRouteTests.class
})
public class AllTestSuite {
}
