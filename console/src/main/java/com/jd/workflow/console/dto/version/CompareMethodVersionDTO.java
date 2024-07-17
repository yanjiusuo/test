package com.jd.workflow.console.dto.version;

import lombok.Data;

/**
 * 方法版本比较
 */
@Data
public class CompareMethodVersionDTO {

    private Long interfaceId;

    private Long methodId;

    private String methodName;

    private String methodCode;

    private String httpMethod;

    private Integer type;

    private String desc;
    /**
     * 当前版本
     */
    private MethodVersionDTO versionDto;
    /**
     * 要比较的版本
     */
    private MethodVersionDTO compareVersionDto;
}
