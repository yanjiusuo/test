package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 * 发布管理列表
 */
@Data
public class PublishManageDTO {

    private Long id;

    /**
     * 发布类型 1-发布生效、0-历史发布
     */
    private Integer isLatest;

    /**
     * 发布后调用地址
     */
    private String address;

    /**
     * 所属的方法id
     */
    private Long relatedMethodId;

    /**
     * 存放的位置 版本id
     */
    private Integer versionId;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 集群id
     */
    private Long clusterId;

    /**
     * camel内容
     */
    private PublishInfoDTO publishInfoDTO;
}
