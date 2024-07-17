package com.jd.workflow.console.dto.auth;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateInterfaceAuthDto {
    /**
     * 应用id
     */
    private Long appId;
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
    /**
     * 接口id
     */
    Long interfaceId;

}
