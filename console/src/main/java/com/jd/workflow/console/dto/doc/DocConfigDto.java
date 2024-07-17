package com.jd.workflow.console.dto.doc;

import lombok.Data;

@Data
public class DocConfigDto {
    String invokeConfig;
    String pomConfig;
    /**
     * 文档类型：md、html
     */
    String docType;
}
