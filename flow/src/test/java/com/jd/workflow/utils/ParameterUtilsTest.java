package com.jd.workflow.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.JsonTypeParseException;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ParameterUtilsTest extends BaseTestCase {
    ParametersUtils utils = new ParametersUtils();
    WorkflowInput newWorkflowInput(){

        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        params.put("pageSize",21);
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(params);
        return workflowInput;
    }
     @Test
     public void testSimpleParam(){

        WorkflowInput workflowInput = newWorkflowInput();
        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);

        Map<String,Object> input = new HashMap<>();
        input.put("pageNo","${workflow.input.params.pageNo}");
        input.put("pageSize","${workflow.input.params.pageSize}");

        Map<String, Object> output = utils.getTaskInput(input, stepContext);
        assertEquals(output.get("pageNo"),workflowInput.getParams().get("pageNo"));
        assertEquals(output.get("pageSize"),workflowInput.getParams().get("pageSize"));
    }
     @Test public void testMvelParam(){
        SimpleJsonType simpleJsonType = new SimpleJsonType();
        simpleJsonType.setExprType(ExprType.script);
        simpleJsonType.setValue("workflow.input.params.pageNo");
        MvelUtils.compileJsonTypeValue(simpleJsonType);


        WorkflowInput workflowInput = newWorkflowInput();

        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);
        Object jsonInputValue = utils.getJsonInputValue(simpleJsonType, stepContext);
        assertEquals(jsonInputValue,workflowInput.getParams().get("pageNo"));
    }
     @Test public void testMvelParam1(){
        SimpleJsonType simpleJsonType = new SimpleJsonType();
        simpleJsonType.setExprType(ExprType.script);
        simpleJsonType.setValue("workflow.input.params.pageNo*workflow.input.params.pageSize");
        MvelUtils.compileJsonTypeValue(simpleJsonType);

        WorkflowInput workflowInput = newWorkflowInput();

        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);
        Object jsonInputValue = utils.getJsonInputValue(simpleJsonType, stepContext);

        MvelUtils.compileJsonTypeValue(simpleJsonType);

        assertEquals(jsonInputValue,(int)workflowInput.getParams().get("pageNo")*(int)workflowInput.getParams().get("pageSize"));
    }
    @Test
    public void testNoChildJsonStringParameterMapper(){
        String type = getResourceContent("classpath:json_type/no_child_json_string_or_array_type.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("personString",JsonUtils.toJSONString(newPerson()));
        body.put("persons",newPersons());
        body.put("personArrayString",JsonUtils.toJSONString(newPersons()));
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonMap1\":\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\",\"id\":null,\"jsonMap2\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonMap1>\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"</jsonMap1><jsonMap2>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap2></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);
    }
    @Test
    public void testJsonStringParameterMapper(){
        String type = getResourceContent("classpath:json_type/json_string_or_array_type.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("persons",newPersons());
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonMap\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\",\"id\":null,\"jsonArray\":\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonMap>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap><jsonArray>\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"</jsonArray></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);

    }

    @Test
    public void testXmlStringParameterMapper(){
        String type = getResourceContent("classpath:json_type/xml_string_or_array_type.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("persons",newPersons());
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonMap\":\"<jsonMap><id>23</id><name>wjf</name></jsonMap>\",\"id\":null,\"jsonArray\":\"<jsonArray><id>23</id><name>wjf</name></jsonArray><jsonArray><id>23</id><name>wjf</name></jsonArray>\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonMap>&lt;jsonMap&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonMap&gt;</jsonMap><jsonArray>&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;</jsonArray></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);

    }

    /**
     * string_json映射为string类型
     */
    @Test
    public void testJsonStringMapperString(){
        String type = getResourceContent("classpath:json_type/json_string_mapper_string.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("personString",JsonUtils.toJSONString(newPerson()));
        body.put("persons",newPersons());
        body.put("personArrayString",JsonUtils.toJSONString(newPersons()));
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonMap4\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\",\"jsonMap1\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\",\"jsonMap3\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\",\"id\":null,\"jsonMap2\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\",\"jsonArray2\":\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\",\"jsonArray1\":\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonMap1>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap1><jsonMap2>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap2><jsonMap3>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap3><jsonMap4>\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}\"</jsonMap4><jsonArray1>\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"</jsonArray1><jsonArray2>\"[{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23},{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":23}]\"</jsonArray2></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);

    }
    @Test
    public void testXmlStringMapperString(){
        String type = getResourceContent("classpath:json_type/xml_string_mapper_string.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        // <jsonMap><id>23</id><name>wjf</name></jsonMap>
        body.put("person",newPerson());
        body.put("id",123);
        body.put("jsonString","<id>123</id>");
        body.put("personString","<jsonMap a=\"1\"><id>23</id><name>wjf</name></jsonMap>");
        body.put("persons",newPersons());
        body.put("personArrayString","<jsonArray b=\"2\"><id>23</id><name>wjf</name></jsonArray><jsonArray ><id>23</id><name>wjf</name></jsonArray>");
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonString1\":\"<id>123</id>\",\"jsonString2\":\"<id>123</id>\",\"jsonMap1\":\"<jsonMap><id>23</id><name>wjf</name></jsonMap>\",\"jsonMap2\":\"<jsonMap><id>23</id><name>wjf</name></jsonMap>\",\"jsonArray2\":\"<jsonArray><id>23</id><name>wjf</name></jsonArray><jsonArray><id>23</id><name>wjf</name></jsonArray>\",\"jsonArray1\":\"<jsonArray><id>23</id><name>wjf</name></jsonArray><jsonArray><id>23</id><name>wjf</name></jsonArray>\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonString2>&lt;id&gt;123&lt;/id&gt;</jsonString2><jsonString1>&lt;id&gt;123&lt;/id&gt;</jsonString1><jsonMap1>&lt;jsonMap&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonMap&gt;</jsonMap1><jsonMap2>&lt;jsonMap&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonMap&gt;</jsonMap2><jsonArray1>&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;</jsonArray1><jsonArray2>&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;</jsonArray2></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);

    }

    @Test
    public void testJsonXmlTypeMapper(){
        String type = getResourceContent("classpath:json_type/string_xml_type.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("id",123);
        body.put("personString",JsonUtils.toJSONString(newPerson()));
        body.put("persons",newPersons());
        body.put("personArrayString",JsonUtils.toJSONString(newPersons()));
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);

        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse((Map)value, option);

        CommonParamMappingUtils.EvalContext evalContext = new CommonParamMappingUtils.EvalContext();
        evalContext.setArgs((Map)value);
        evalContext.setDocumentContext(documentContext);

        Object jsonStr = utils.replace("${$.jsonString}",evalContext);
        Object jsonMap = utils.replace("${$.jsonMap.jsonMap.id}",evalContext);
        Object jsonArray = utils.replace("${$.jsonArray.jsonArray[0].id}",evalContext);
        assertEquals("\"<id>123</id>\"",JsonUtils.toJSONString(jsonStr));
        assertEquals(23,jsonMap);
        assertEquals(23,jsonArray);
    }

    @Test
    public void testStringJsonTypeMapper(){
        String type = getResourceContent("classpath:json_type/string_json_type.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("id",123);
        body.put("personString",JsonUtils.toJSONString(newPerson()));
        body.put("persons",newPersons());
        body.put("personArrayString",JsonUtils.toJSONString(newPersons()));
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);

        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse((Map)value, option);

        CommonParamMappingUtils.EvalContext evalContext = new CommonParamMappingUtils.EvalContext();
        evalContext.setArgs((Map)value);
        evalContext.setDocumentContext(documentContext);

        Object jsonStr = utils.replace("${$.jsonString}",evalContext);
        Object jsonMap = utils.replace("${$.jsonMap.id}",evalContext);
        Object jsonArray = utils.replace("${$.jsonArray[0].id}",evalContext);
        assertEquals("123",JsonUtils.toJSONString(jsonStr));
        assertEquals(23,jsonMap);
        assertEquals(23,jsonArray);
    }
    @Test(expected = JsonTypeParseException.class)
    public void testXmlStringNoChild(){
        String type = getResourceContent("classpath:json_type/xml_string_no_child.json");
        JsonType jsonType = JsonUtils.parse(type, JsonType.class);
        ParametersUtils utils = new ParametersUtils();
        Map<String,Object> body = new HashMap<>();
        body.put("person",newPerson());
        body.put("persons",newPersons());
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(body);
        stepContext.setInput(workflowInput);
        Object value = utils.getJsonInputValue(jsonType, stepContext);
        String jsonStr = JsonUtils.toJSONString(value);
        assertEquals("{\"jsonMap\":\"<jsonMap><id>23</id><name>wjf</name></jsonMap>\",\"id\":null,\"jsonArray\":\"<jsonArray><id>23</id><name>wjf</name></jsonArray><jsonArray><id>23</id><name>wjf</name></jsonArray>\"}",jsonStr);
        String xml  = XNode.toXml(jsonType.transformToXml(value));
        assertEquals("<root><jsonMap>&lt;jsonMap&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonMap&gt;</jsonMap><jsonArray>&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;&lt;jsonArray&gt;&lt;id&gt;23&lt;/id&gt;&lt;name&gt;wjf&lt;/name&gt;&lt;/jsonArray&gt;</jsonArray></root>",xml);
        log.info("json_Str={}",jsonStr);
        log.info("xml_str={}",xml);

    }
    private Object newPerson() {
        Map person = new HashMap();
        person.put("id",23);
        person.put("name","wjf");
        return person;
    }
    private Object newPersons() {
        Map person = new HashMap();
        person.put("id",23);
        person.put("name","wjf");
        List persons = new ArrayList<>();
        persons.add(person);
        persons.add(person);
        return persons;
    }

    public static class Person{
        String id;
        String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @Test
    public void testJsonValue(){
        Person person = new Person();
        person.setId("id");
        person.setName("wjf");

        StepContext stepContext = new StepContext();
        WorkflowInput input = new WorkflowInput();
        stepContext.setInput(input);

        HttpOutput httpOutput = new HttpOutput();
        httpOutput.setBody(person);
        Step http = new Step();
        http.setId("http");
        http.setInput(new HttpInput());
        http.setOutput(httpOutput);
        stepContext.registerStep(http);

        Map<String,Object> inputMap = new HashMap<>();
        inputMap.put("personId","${steps.http.output.body.id}");

        ParametersUtils utils = new ParametersUtils();
        final Map<String, Object> taskInput = utils.getTaskInput(inputMap, stepContext);
        assertEquals("{\"personId\":\"id\"}",JsonUtils.toJSONString(taskInput));
        System.out.println(taskInput);
    }
}
