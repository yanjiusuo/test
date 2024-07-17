package com.jd.workflow.console.dto.auth;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JsfInterfaceAuthDto {
    private Long id;
    String name;
    String serviceCode;
    /**
     * 产品负责人
     */
    private List<String> productManagerList;
    /**
     * 研发负责人
     */
    private String developmentManager;
    /**
     * 研发相关人员
     */
    private List<String> developerList = new ArrayList();
    private String updated;

}
