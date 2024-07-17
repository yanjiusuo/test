package com.jd.workflow.console.dto.manage;

import lombok.Data;

import java.util.List;
@Data
public class JsfInterfaceAndMethod {
    /**
     * jsf接口名
     */
    String jsfName;
    /**
     * 方法列表
     */
    List<String> methodNames;
}
