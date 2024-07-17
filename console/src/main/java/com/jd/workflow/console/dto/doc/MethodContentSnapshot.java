package com.jd.workflow.console.dto.doc;

import lombok.Data;

@Data
public class MethodContentSnapshot {
    String desc;
    String content;
    /**
     * http 方法列表，以,分割
     */
    String httpMethod;
    String inputExample;
    String outputExample;
}
