package com.jd.workflow.xml;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SchemaTypeToJsonTypeTests.class,
        SoapInputToJsonTests.class,
        WsdlSchemaTypeToJsonTests.class
})
public class XmlTestSuite {
}
