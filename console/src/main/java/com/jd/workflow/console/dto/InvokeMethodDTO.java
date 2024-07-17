package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvokeMethodDTO {
    /**
     * 类型
     */
    String type;
    /**
     * 方法id
     */
    Long methodId;
    /**
     * 环境名称
     */
    String envName;

    /**
     * input
     */
    Map input;
    /**
     * 成功条件
     */
    String successCondition;
}
