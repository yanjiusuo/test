package com.jd.workflow.console.dto.auth;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppAuthDto {
    /**
     * 应用id
     */
    private Long appId;

    /**
     * 鉴权级别 0 接口 1方法
     */
    private String authLevel;
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
     * 接口列表
     */
    List<Long> interfaceIds;

}
