package com.jd.workflow.utils;

import com.jd.workflow.xml.SchemaTypeToJsonTypeTests;
import com.jd.workflow.xml.SoapInputToJsonTests;
import com.jd.workflow.xml.WsdlSchemaTypeToJsonTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ParameterUtilsTest.class,
        XmlUtilsTest.class,
        TransformUtilsTest.class
})
public class UtilsTestSuite {
}
