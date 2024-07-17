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
public class HttpAuthApplyDetailDTO  implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 站点
     */
    private String site;

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
    @ExcelProperty(value = "应用名称（接口调用方）", index = 2)
    private String callAppName;

    /**
     * 调用方应用编码
     */
    @ExcelProperty(value = "*应用编码（接口调用方）", index = 3)
    private String callAppCode;

    /**
     * 鉴权标识
     */
    @ExcelProperty(value = "*鉴权标识", index = 4)
    private String authCode;

    /**
     * 项目ID
     */
    @ExcelProperty(value = "*申请的项目ID", index = 5)
    private Long interfaceId;

    /**
     * 方法路径
     */
    @ExcelProperty(value = "*接口路径", index = 6)
    private String path;

    /**
     * 方法名称
     */
    @ExcelProperty(value = "*接口名称", index = 7)
    private String methodName;

    /**
     * 接口编码
     */
    private String interfaceCode;
    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 方法ID
     */
    private Long methodId;

    /**
     * 方法编码
     */
    private String methodCode;

    /**
     * 创建者
     */
    private String creator;

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
     * 申请单ID
     */
    private String ticketId;


    /**
     * token
     */
    private String token;


    /**
     * 负责人名称
     */
    private String ownerName;


    /**
     * 负责人erp
     */
    private String ownerErp;

    /**
     * 申请方法列表
     */
    private List<HttpAuthApplyDetailDTO> methodList;

    /**
     * 导入失败原因
     */
    private String failMsg;

    /**
     * 接口分享时，文件夹类型 1：方法  2：方法分组
     */
    private int type;
}
