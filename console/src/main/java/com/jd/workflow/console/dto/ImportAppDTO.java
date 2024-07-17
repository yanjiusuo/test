package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.List;

/**
 * 导入的app
 */
@Data
public class ImportAppDTO {

    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;

    /**
     * 负责人
     */
    private List<String> owner;

    /**
     * 应用成员
     */
    private List<String> member;

    /**
     * 产品负责人
     */
    private List<String> productor;

    /**
     * 测试负责人
     */
    private List<String> tester;

    /**
     * 测试成员
     */
    private List<String> testMember;

    /**
     * 调用级别 0 接口 1方法
     */
    private String authLevel;

}
