package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.List;

@Data
public class InterfaceQueryDto  {
    String adminCode;
    String currentUser;
    Integer[] types;
    Integer resourceType;
    String name;
    Integer nodeType;
    String tenantId;
    boolean authInterface;
    Long appId;
    /**
     * 当前页号
     */
    private Long offset=0L;
    /**
     * 页大小 最大500条
     */
    private Long limit=10L;
    private boolean publicInterface;

    private String deptName;
    //是否编排接口
   /* private boolean publicInterface;*/
    /**
     * 接口分级
     */
    private Integer level;
    /**
     * 是否自动上报
     */
    private Integer autoReport;
}
