package com.jd.workflow.flow;

import com.jd.workflow.flow.parser.StepTopoGraphTests;
import com.jd.workflow.flow.parser.WorkflowParserTests;
import com.jd.workflow.mvel.MvelExprTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StepTopoGraphTests.class,
        WorkflowParserTests.class,
        CamelStepRouteTests.class,
        SubflowTests.class,
        ChoiceRouteTests.class
})
public class FlowTestSuite {
}
