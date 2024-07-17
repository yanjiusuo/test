package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.stream.Collectors;

/**
 * 专门用来构造伪代码
 */
public class PseudoBuildUtils {
    static String DEFAULT_CODE = "function callHttp(stepId){ // http调用逻辑，其他函数逻辑与此类似\n" +
            "\tvar fallbackContent = getFallbackContent(stepId);\n" +
            "\ttry{\n" +
            "\t steps[stepId].output = call({\"stepId\":stepId});\n" +
            "\t if(!isSuccess(steps[stepId].output)){ // 是否执行成功判别\t\t\n" +
            "\t\t  if(fallbackContent != null){\n" +
            "\t\t\tthrow new Exception(\"执行失败\");\n" +
            "\t\t  }\n" +
            "\t\t  steps[stepId].output = fallbackContent;//用fallback做内容\n" +
            "\t }\n" +
            "\t}catch(Exception e){\n" +
            "\t  if(fallbackContent != null){\n" +
            "\t    throw e;\n" +
            "\t  }\n" +
            "\t  steps[stepId].output = fallbackContent;//用fallback做内容\n" +
            "\t}\n" +
            "}";
    public static String buildPseudoCode(WorkflowDefinition definition){
        String template = "try{\n" +
                "  %s\n" +
                "  collectOutput();// 收集正确输出值\n" +
                "}catch(Exception e){\n" +
                "  collectFailOutput();// 收集错误输出值\n" +
                "}";
        String childCode = definition.getTasks().stream().map(task->task.getMetadata().buildPseudoCode()).collect(Collectors.joining("   \n"));
        String code = String.format(template,childCode);
        code+="\n"+DEFAULT_CODE;
        return code;
    }
}
