package com.jd.workflow.flow.parser;

import com.jd.workflow.flow.core.definition.*;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.exception.WorkflowParserException;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.parser.context.impl.DefaultFlowParserContext;
import com.jd.workflow.flow.utils.TypeConverterUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.flow.utils.ParserUtils;
import com.jd.workflow.soap.common.exception.StdException;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WorkflowParser {
    static final Logger logger = LoggerFactory.getLogger(WorkflowParser.class);
    public static WorkflowDefinition parse(Map<String,Object> result){
        return parse(result,true);
    }
    public static WorkflowDefinition parse(Map<String,Object> result,boolean validate){
        return parse(result,validate,false);
    }
    public static WorkflowDefinition parse(Map<String,Object> result, IFlowParserContext flowParserContext){
        if(flowParserContext == null){
            flowParserContext = new DefaultFlowParserContext();
        }
        if(result == null){
            throw new StepParseException("task.err_task_def_not_allow_empty");
        }
        boolean validate = flowParserContext.isValidate();
        boolean ignoreErrorStep = flowParserContext.isIgnoreErrorStep();
        try {

            Map<String,Object> input = (Map<String, Object>) result.get("input");
            Map<String,Object> output = (Map<String, Object>) result.get("output");
            Map<String,Object> failOutput = (Map<String, Object>) result.get("failOutput");
            List<Map<String,Object>> params = (List<Map<String, Object>>) result.get("params");
            if(params == null){
                params = new ArrayList<>();
            }
            Object tasks = result.get("tasks");

            WorkflowDefinition workflow = new WorkflowDefinition();
            List<WorkflowParam> flowParams = new ArrayList<>();
            for (Map<String, Object> param : params) {
                WorkflowParam flowParam = new WorkflowParam();
                Long entityId = Variant.valueOf(param.get("entityId")).toLong(null);
                flowParam.setEntityId(entityId);
                flowParam.setName((String) param.get("name"));
                String value = Variant.valueOf(param.get("value")).toString();
                flowParam.setValue(value);
                flowParams.add(flowParam);
            }
            Boolean hasJavaBean = (Boolean) result.get("hasJavaBean");
            if(hasJavaBean != null){
                workflow.setHasJavaBean(hasJavaBean);
            }
            workflow.setParams(flowParams);
            workflow.setInput(TypeConverterUtils.cast(input, WorkflowInputDefinition.class,"workflow.input"));
            workflow.setOutput(TypeConverterUtils.cast(output, WorkflowOutputDefinition.class,"workflow.output"));
            workflow.setFailOutput(TypeConverterUtils.cast(failOutput, WorkflowOutputDefinition.class,"workflow.failOutput"));




            if(ObjectHelper.isEmpty(tasks)){
                /*if(validate){
                    throw new WorkflowParserException("workflow.err_tasks_is_not_allow_empty");
                }else{*/
                return workflow;
                //}
            }
            if(!(tasks instanceof List)) throw new WorkflowParserException("workflow.err_tasks_only_allow_list");

            Set<String> keys = new HashSet<>();
            List<Map> taskList = (List<Map>) tasks;
            for (Map task : taskList) {
                try{
                    StepDefinition taskDef = parseStep(task,flowParserContext,validate);
                    workflow.getTasks().add(taskDef);
                }catch (Exception e){
                    if(!ignoreErrorStep){
                        throw e;
                    }
                }

            }

            if(validate){
                validateUniqueId(workflow);
                workflow.init();
            }

            return workflow;
        } catch (StepParseException e) {
            logger.error("workflow.err_parse_text:jsonConfig={}",JsonUtils.toJSONString(result),e);
            throw e;
        }
    }
    public static WorkflowDefinition parse(Map<String,Object> result,boolean validate,boolean ignoreErrorStep){
        IFlowParserContext context = new DefaultFlowParserContext();
        context.setIgnoreErrorStep(ignoreErrorStep);
        context.setValidate(validate);
        return parse(result,context);
    }
    public static WorkflowDefinition parse(String jsonConfig){
        ParserUtils.notEmpty(jsonConfig,"task.err_task_def_not_allow_empty");
        Map result = JsonUtils.parse(jsonConfig, Map.class);
        return parse(result,true);
    }
    public static WorkflowDefinition parse(String jsonConfig,boolean validate){
        ParserUtils.notEmpty(jsonConfig,"task.err_task_def_not_allow_empty");
        Map result = JsonUtils.parse(jsonConfig, Map.class);
        return parse(result,validate);
    }
    private static void validateUniqueId(WorkflowDefinition workflow){
        StepTopoGraph graph = new StepTopoGraph();
        graph.setDefinitions(workflow.getTasks());
        graph.init();
        graph.validateUnique();
    }
    public static StepDefinition parseStep(Map<String,Object> task,IFlowParserContext parserContext){
        return parseStep(task,parserContext,true);
    }
    public static StepDefinition parseStep(Map<String,Object> task,IFlowParserContext parserContext,boolean init){
        ParserUtils.notEmpty(task,"task.err_task_step_not_allow_empty");
        ParserUtils.notEmpty(task.get("id"),"task.err_task_id_not_allow_empty");
        ParserUtils.notEmpty(task.get("type"),"task.err_task_type_not_allow_empty");
        String type = (String) task.get("type");
        Class<? extends StepDefinition>  clazz= StepParserRegistry.getType(type);
        if(clazz == null){
            clazz= StepParserRegistry.getType("default");
        }
        try{
            StepDefinition def = clazz.newInstance();
            def.setId((String) task.get("id"));
            def.parseMetadata(task,parserContext,init);
            return def;
        }catch (StepParseException e){
            throw e;
        }catch (Exception e){
            logger.error("workflow.err_init_task",e);
            throw StdException.adapt("workflow.err_init_task",e);
        }

    }
    public static List<StepDefinition> parseSteps(List<Map<String,Object>> tasks,IFlowParserContext parserContext){
        if(tasks == null || tasks.isEmpty()) return new ArrayList<>();
        List<StepDefinition> defs = new ArrayList<>();
        for (Map<String, Object> task : tasks) {
            defs.add(parseStep(task,parserContext));
        }
        return defs;
    }
}
