package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 * 项目名称：parent
 * 类 名 称：PublishMethodDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-28 13:53
 * 创 建 人：wangxiaofei8
 */
@Data
public class PublishMethodDTO {

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
     * 方法名称
     */
    private String methodName;

    /**
     * 方法code
     */
    private String methodCode;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口服务code
     */
    private String serviceCode;

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
     * 集群域名
     */
    private String clusterDomain;
    /**
     * 方法类型
     */
    private Integer type;

}
