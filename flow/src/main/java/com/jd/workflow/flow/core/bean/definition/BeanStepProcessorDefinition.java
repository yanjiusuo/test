package com.jd.workflow.flow.core.bean.definition;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

/**
 * bean处理器定义
 */
@Data
public class BeanStepProcessorDefinition {
    String stepName;
    List<JsonType> initArgs;
    List<BeanMethodDefinition> methods;
}
