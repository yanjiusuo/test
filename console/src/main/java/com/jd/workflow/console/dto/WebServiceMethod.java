package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存在数据库里的content
 * @date: 2022/5/25 18:07
 * @author wubaizhao1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebServiceMethod {
    /**
     * 类型
     */
    String type;
    /**
     * 方法名
     */
    String methodName;
    /**
     * 方法id
     */
    Long methodId;
    /**
     * 环境名称
     */
    String envName;

    String soapAction;
    /**
     * 成功条件
     */
    String successCondition;

    /**
     * @date: 2022/5/25 18:09
     * @author wubaizhao1
     */
    WebServiceMethodIO input;
    /**
     * @date: 2022/5/25 18:09
     * @author wubaizhao1
     */
    WebServiceMethodIO output;


    /**
     * @date: 2022/5/25 18:08
     * @author wubaizhao1
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WebServiceMethodIO{
        String demoXml;
        JsonType schemaType;

    }
}
