package com.jd.workflow.flow.core.camel;

import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.definition.WorkflowOutputDefinition;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import org.apache.camel.CamelContext;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RouteBuilder {
    public static String STEP_SEPARATOR = ":";
    public static XNode makeBeanNode(String prefix,String id,Object description){
        XNode bean = XNode.make("bean");
        bean.attr("beanType","com.jd.workflow.flow.core.camel.CamelStepBean")
                .attr("id", prefix+id)
                .makeChild("description")
                .markCDATA(true).content(JsonUtils.toJSONString(description));
        return bean;
    }
    //public static String buildRoutes(){}
    public static String buildRoute(WorkflowDefinition definition){
        XNode root = XNode.make("routes").attr("xmlns","http://camel.apache.org/schema/spring");
        buildRoute(definition,root,null);
        return root.toXml();
    }
    //public static String buildRoutes(){}
    public static XNode buildRoute(WorkflowDefinition definition,XNode rootNode,String id){
        //List<RouteDefinition> routeDefs = new ArrayList<>();
        /*FromDefinition fromDef = new FromDefinition("direct:start");

        RouteDefinition routeDef = new RouteDefinition();
        routeDef.setInput(fromDef);*/

        XNode parent = XNode.make("route").attr("xmlns","http://camel.apache.org/schema/spring");
        rootNode.appendChild(parent);
        String idPrefix = "";
        if(!StringUtils.isEmpty(id)){
            idPrefix = id+STEP_SEPARATOR;
            parent.makeChild("from").attr("uri","direct:"+RouteStepBuilder.SUB_FLOW_PREFIX+id);
        }else{
            parent.makeChild("from").attr("uri","direct:start");
        }
        XNode tryNode = parent.makeChild("doTry");
        if(definition.getParams() != null){
            Map<String,Object> params = new HashMap<>();
            params.put("type","setParam");
            params.put("params",definition.getParams());
            tryNode.appendChild(makeBeanNode(idPrefix,"_setParam",params));
        }
        tryNode.appendChild(inputValidateNode(idPrefix,definition ));

        for (StepDefinition task : definition.getTasks()) {
            task.build(tryNode,rootNode,idPrefix);
        }

        if(definition.getOutput() != null && !definition.getOutput().isEmpty() ){
            tryNode.appendChild(outputCollectNode(idPrefix,definition.getOutput()));
        }


        XNode doCatch = XNode.make("doCatch");
        doCatch.makeChild("exception").content("java.lang.Exception");
        Map<String, Object> exceptionData = exceptionData(definition);

        doCatch.appendChild(makeBeanNode(idPrefix,(String) exceptionData.get("id"),exceptionData));
        tryNode.appendChild(doCatch);
        return parent;
    }
    /**
     * 输出收集节点
     * @return
     */
    static XNode outputCollectNode(String prefix,WorkflowOutputDefinition output){
        Map<String,Object> map = new HashMap<>();
        map.put("id","__outCollect");
        map.put("output",output);
        map.put("type","collect");
        return makeBeanNode(prefix,"__result_collect",map);
    }

    static XNode inputValidateNode(String prefix,WorkflowDefinition definition){

        Map<String, Object> map = inputValidateData(definition);
        XNode bean = makeBeanNode(prefix,(String) map.get("id"),map);

        return bean;
    }
    static Map<String,Object> inputValidateData(WorkflowDefinition definition){
        Map<String,Object> map = new HashMap<>();
        map.put("id","__validate");
        map.put("input",definition.getInput());
        map.put("type","reqValidate");
        return map;
    }
   static Map<String,Object> exceptionData(WorkflowDefinition definition){
        Map<String,Object> map = new HashMap<>();
        map.put("id","__exception");
        map.put("output",definition.getFailOutput());
        map.put("type","exception");
        return map;
    }

    public static void main(String[] args) {
        XNode route = XNode.make("route").attr("xmlns", "http://camel.apache.org/schema/spring");
        route.makeChild("child");
        route.makeChild("child1");
        String result = route.toXml();
        System.out.println(result);
    }

}
