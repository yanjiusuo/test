package com.jd.workflow.soap.common.method;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class MethodMetadata {
    private Long id;
    /**
     * 入参
     */
    List<? extends JsonType> input;

    /**
     * 出参
     */
    JsonType output;
    String interfaceName;
    String methodName;

    String[] exceptions;
    String cnName;
    String desc;
    String functionId;

}
