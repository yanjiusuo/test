package com.jd.workflow.console.dto.doc;

import lombok.Data;

@Data
public class JsfAndHttpInterfaceCountDto {
    /**
     * JSF接口数量
     */
    Long jsfInterfaceCount;
    /**
     * HTTP接口数量
     */
    Long httpInterfaceCount;
}
