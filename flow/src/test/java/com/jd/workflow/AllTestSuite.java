package com.jd.workflow;

import com.jd.workflow.flow.FlowTestSuite;
import com.jd.workflow.mvel.MvelTestSuite;

import com.jd.workflow.processor.ProcessorTestSuite;
 
import com.jd.workflow.utils.UtilsTestSuite;
import com.jd.workflow.xml.XmlTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        XmlTestSuite.class,
        UtilsTestSuite.class,
        ProcessorTestSuite.class,
        MvelTestSuite.class,
        FlowTestSuite.class,
})
public class AllTestSuite {

}
