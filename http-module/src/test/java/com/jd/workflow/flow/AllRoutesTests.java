package com.jd.workflow.flow;

import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.service.FullTypedWebService;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.apache.cxf.endpoint.Server;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AllRoutesTests extends HttpBaseTestCase {
    @Test
    public void testHttp() throws Exception {
        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();

        DefaultCamelContext camelContext = new DefaultCamelContext();



        StepContext stepContext = new StepContext();
        stepContext.setInput(new WorkflowInput());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());
        camelContext.addRouteDefinitions(camelRouteLoader.loadRoutesFromPath("camel/camel-step-bean.xml"));
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
    @Test public void testXmlTransform(){

        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("sid",123);
        body.put("name","name");

        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/single-flow.json");
        assertEquals("{\"sid\":123,\"name\":\"name\"}", JsonUtils.toJSONString(result.getBody()));
    }

    /**
     * 将2个步骤的查询结果合并，并重新生成新的步骤
     */
    @Test public void testHttpDemo1(){

        WorkflowInput workflowInput = new WorkflowInput();

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("id1",1);
        body.put("id2",2);

        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo1.json");
        assertEquals("{\"code\":\"0\",\"data\":[{\"id\":1,\"html\":\"<div style='color:red'>这是一段html代码</div>\"},{\"id\":2,\"html\":\"<div style='color:blue'>这是又一段html代码</div>\"}],\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
    }
    /**
     * 将2个步骤的查询结果合并，并重新生成新的步骤
     */
    @Test public void testHttpDemo2(){

        WorkflowInput workflowInput = new WorkflowInput();

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("id",1);


        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo2.json");
        assertEquals("{\"code\":\"0\",\"data\":\"这是一段html代码\",\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
    }
    @Test public void testHttpDemo3(){

        WorkflowInput workflowInput = new WorkflowInput();

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("cityId",11);


        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo3.json");
        assertEquals("{\"code\":\"0\",\"data\":\"河北省\",\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
    }
    /**
     *
     */
    @Test public void testHttpDemo4(){

        WorkflowInput workflowInput = new WorkflowInput();

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("date","2021-11-21");
        body.put("sex","1");
        body.put("json","{\"a\":1}");
        body.put("xml","<person><id>321</id><name>wjf</name></person>");

        System.out.println(JsonUtils.toJSONString(body));

        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo4.json");
        assertEquals("{\"date\":\"2021年11月21日\",\"sex\":\"男\",\"json\":{\"a\":1},\"xml\":{\"person\":{\"id\":\"321\",\"name\":\"wjf\"}}}",JsonUtils.toJSONString(result.getBody()));
    }
    public static  String WEBSERVICE_URL = "http://127.0.0.1:7001/FullTypedWebService";

    @Test public void testHttpDemo5(){
        Server server = FullTypedWebService.run(null, WEBSERVICE_URL);
        try{
            WorkflowInput workflowInput = new WorkflowInput();

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("roleName","admin");
            params.put("pageNo",1);
            params.put("userSystem",1);
            params.put("pageSize",10);

            System.out.println(JsonUtils.toJSONString(params));

            workflowInput.setParams(params);

            HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo5.json");
            assertEquals("{\"code\":\"0\",\"data\":{\"roleInfo\":{\"createBy\":\"admin\",\"createDate\":\"2022年06月29日\",\"id\":1,\"level\":\"1\",\"roleDesc\":\"管理员\",\"roleName\":\"admin\"},\"userList\":[{\"id\":\"1\",\"roleName\":\"admin\",\"userName\":\"name1\",\"userNick\":\"用户1\",\"createDate\":\"2022-01-01\"},{\"id\":\"2\",\"roleName\":\"admin\",\"userName\":\"name2\",\"userNick\":\"用户2\",\"createDate\":\"2022-01-01\"},{\"id\":\"5\",\"roleName\":\"admin\",\"userName\":\"name6\",\"userNick\":\"用户5\",\"createDate\":\"2022-01-01\"},{\"id\":\"6\",\"roleName\":\"admin\",\"userName\":\"name7\",\"userNick\":\"用户6\",\"createDate\":\"2022-01-01\"}],\"subRoleList\":{\"pageNo\":1,\"pageSize\":10,\"total\":19,\"items\":[{\"id\":1,\"name\":\"subRole1\",\"subRoleNick\":\"子角色1\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":2,\"name\":\"subRole2\",\"subRoleNick\":\"子角色2\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":3,\"name\":\"subRole3\",\"subRoleNick\":\"子角色3\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":4,\"name\":\"subRole4\",\"subRoleNick\":\"子角色4\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":5,\"name\":\"subRole5\",\"subRoleNick\":\"子角色5\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":6,\"name\":\"subRole6\",\"subRoleNick\":\"子角色6\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":7,\"name\":\"subRole7\",\"subRoleNick\":\"子角色7\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":8,\"name\":\"subRole8\",\"subRoleNick\":\"子角色8\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":9,\"name\":\"subRole9\",\"subRoleNick\":\"子角色9\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":10,\"name\":\"subRole10\",\"subRoleNick\":\"子角色10\",\"roleName\":\"admin\",\"roleId\":\"1\"}]}},\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
        }finally {
            server.stop();
        }

    }

    /*@Test public void testHttpDemo6(){
        Server server = FullTypedWebService.run(null, WEBSERVICE_URL);
        try{
            WorkflowInput workflowInput = new WorkflowInput();

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("roleName","admin");
            params.put("pageNo",1);
            params.put("userSystem",1);
            params.put("pageSize",10);

            System.out.println(JsonUtils.toJSONString(params));

            workflowInput.setParams(params);

            HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo6.json");
            assertEquals("{\"code\":\"0\",\"data\":{\"roleInfo\":{\"createBy\":\"admin\",\"createDate\":\"2022年06月29日\",\"id\":1,\"level\":\"1\",\"roleDesc\":\"管理员\",\"roleName\":\"admin\"},\"userList\":[{\"id\":\"1\",\"roleName\":\"admin\",\"userName\":\"name1\",\"userNick\":\"用户1\",\"createDate\":\"2022-01-01\"},{\"id\":\"2\",\"roleName\":\"admin\",\"userName\":\"name2\",\"userNick\":\"用户2\",\"createDate\":\"2022-01-01\"},{\"id\":\"5\",\"roleName\":\"admin\",\"userName\":\"name6\",\"userNick\":\"用户5\",\"createDate\":\"2022-01-01\"},{\"id\":\"6\",\"roleName\":\"admin\",\"userName\":\"name7\",\"userNick\":\"用户6\",\"createDate\":\"2022-01-01\"}],\"subRoleList\":{\"pageNo\":1,\"pageSize\":10,\"total\":19,\"items\":[{\"id\":1,\"name\":\"subRole1\",\"subRoleNick\":\"子角色1\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":2,\"name\":\"subRole2\",\"subRoleNick\":\"子角色2\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":3,\"name\":\"subRole3\",\"subRoleNick\":\"子角色3\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":4,\"name\":\"subRole4\",\"subRoleNick\":\"子角色4\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":5,\"name\":\"subRole5\",\"subRoleNick\":\"子角色5\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":6,\"name\":\"subRole6\",\"subRoleNick\":\"子角色6\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":7,\"name\":\"subRole7\",\"subRoleNick\":\"子角色7\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":8,\"name\":\"subRole8\",\"subRoleNick\":\"子角色8\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":9,\"name\":\"subRole9\",\"subRoleNick\":\"子角色9\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":10,\"name\":\"subRole10\",\"subRoleNick\":\"子角色10\",\"roleName\":\"admin\",\"roleId\":\"1\"}]}},\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
        }finally {
            server.stop();
        }

    }
    @Test public void testHttpDemo7(){

        try{
            WorkflowInput workflowInput = new WorkflowInput();

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("address","abc123");

            Map<String,Object> headers = new LinkedHashMap<>();
            headers.put("id","1");

            Map<String,Object> body = new LinkedHashMap<>();
            body.put("body","1");

            System.out.println(JsonUtils.toJSONString(params));
            workflowInput.setBody(body);
            workflowInput.setHeaders(headers);
            workflowInput.setParams(params);

            HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/demo/http-demo7.json");
            assertEquals("{\"code\":\"0\",\"data\":{\"roleInfo\":{\"createBy\":\"admin\",\"createDate\":\"2022年06月29日\",\"id\":1,\"level\":\"1\",\"roleDesc\":\"管理员\",\"roleName\":\"admin\"},\"userList\":[{\"id\":\"1\",\"roleName\":\"admin\",\"userName\":\"name1\",\"userNick\":\"用户1\",\"createDate\":\"2022-01-01\"},{\"id\":\"2\",\"roleName\":\"admin\",\"userName\":\"name2\",\"userNick\":\"用户2\",\"createDate\":\"2022-01-01\"},{\"id\":\"5\",\"roleName\":\"admin\",\"userName\":\"name6\",\"userNick\":\"用户5\",\"createDate\":\"2022-01-01\"},{\"id\":\"6\",\"roleName\":\"admin\",\"userName\":\"name7\",\"userNick\":\"用户6\",\"createDate\":\"2022-01-01\"}],\"subRoleList\":{\"pageNo\":1,\"pageSize\":10,\"total\":19,\"items\":[{\"id\":1,\"name\":\"subRole1\",\"subRoleNick\":\"子角色1\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":2,\"name\":\"subRole2\",\"subRoleNick\":\"子角色2\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":3,\"name\":\"subRole3\",\"subRoleNick\":\"子角色3\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":4,\"name\":\"subRole4\",\"subRoleNick\":\"子角色4\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":5,\"name\":\"subRole5\",\"subRoleNick\":\"子角色5\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":6,\"name\":\"subRole6\",\"subRoleNick\":\"子角色6\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":7,\"name\":\"subRole7\",\"subRoleNick\":\"子角色7\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":8,\"name\":\"subRole8\",\"subRoleNick\":\"子角色8\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":9,\"name\":\"subRole9\",\"subRoleNick\":\"子角色9\",\"roleName\":\"admin\",\"roleId\":\"1\"},{\"id\":10,\"name\":\"subRole10\",\"subRoleNick\":\"子角色10\",\"roleName\":\"admin\",\"roleId\":\"1\"}]}},\"message\":\"获取成功\"}",JsonUtils.toJSONString(result.getBody()));
        }finally {

        }

    }*/
}
