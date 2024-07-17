package com.jd.workflow.console.dto.usecase;

import lombok.Data;

import java.util.List;

@Data
public class RequiremenUnderInterfacesDTO {
    /**
     * JSF的用例列表
     */
    List<TreeItem> JSFCases;

    /**
     * HTTP的用例列表
     */
    List<TreeItem> HTTPCases;

}
