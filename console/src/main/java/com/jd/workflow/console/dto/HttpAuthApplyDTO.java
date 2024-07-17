package com.jd.workflow.console.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 鉴权标签管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
public class HttpAuthApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 应用名称
     */
    @ExcelProperty(value = "应用名称（接口提供方）", index = 0)
    private String appName;

    /**
     * 应用编码
     */
    @ExcelProperty(value = "*应用编码（接口提供方）", index = 1)
    private String appCode;

    /**
     * 调用方应用名称
     */
    @ExcelProperty(value = "被授权应用名称（接口调用方）", index = 2)
    private String callAppName;

    /**
     * 调用方应用编码
     */
    @ExcelProperty(value = "*被授权应用编码（接口调用方）", index = 3)
    private String callAppCode;

    /**
     * 创建者
     */
    @ExcelProperty(value = "*被授权负责人（接口调用方）", index = 4)
    private String creator;

    /**
     * 鉴权标识
     */
    @ExcelProperty(value = "*鉴权标识", index = 5)
    private String authCode;

    /**
     * token
     */
    @ExcelProperty(value = "*授权token", index = 6)
    private String token;

    /**
     * 站点
     */
    private String site;

    /**
     * 修改者
     */
    private String modifier;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    private Integer yn;


    /**
     * 是否推送ducc成功，0无效，1有效
     */
    private Integer duccStatus;

    /**
     * 状态，审批状态
     */
    private Integer ticketStatus;

    /**
     * 申请单ID
     */
    private String ticketId;

    /**
     * 申请明细
     */
    private List<HttpAuthApplyDetailDTO> interfaceList;

    /**
     * 产品审批人
     */
    private List<String> productApprovers;

    /**
     * 研发审批人
     */
    private List<String> devApprovers;

    /**
     * 申请原因
     */
    private String applyDesc;

    /**
     * 负责人名称
     */
    private String ownerName;

    /**
     * 负责人erp
     */
    private String ownerErp;

    /**
     * 导入失败原因
     */
    private String failMsg;

}
