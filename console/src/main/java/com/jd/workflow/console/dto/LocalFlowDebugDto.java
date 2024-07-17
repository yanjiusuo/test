package com.jd.workflow.console.dto;

import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 本地工作流调试数据
 */
@Data
public class LocalFlowDebugDto {
    /**
     * 工作流定义文件：{
     *     tasks:[{}]
     *
     * }
     */
    Map<String,Object> flowDef;
    /**
     * 入参
     */
    WorkflowInputDefinition inputDef;
}
