package com.jd.workflow.soap;

import com.jd.workflow.soap.classinfo.TestClassGenerator;
import com.jd.workflow.soap.classinfo.TestHttpWsdlGenerator;
import com.jd.workflow.soap.classinfo.TestWsdlGenerator;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
       // TestClassGenerator.class,
        TestHttpWsdlGenerator.class,
        TestWsdlGenerator.class
})
public class WsdlTestSuite {
}
