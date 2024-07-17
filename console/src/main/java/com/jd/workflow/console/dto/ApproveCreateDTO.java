package com.jd.workflow.console.dto;

import lombok.Data;

@Data
public class ApproveCreateDTO {
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 应用code
     *
     */
    private String appCode;

    private Long appId;

    /**
     * 接口id /方法id
     */
    private Long sourceId;

    /**
     * 文档链接
     */
    private String docLink;
    /**
     *审批内容 tags
     */
    private String contents;
    /**
     * type类型 0=接口 1=方法
     */
    private Integer type;
    /**
     * 贡献人
     */
    private String contributor;

    /**
     * 状态 0=审核中(默认) 1=已关联 2=已驳回
     */
    private Integer status;
}
