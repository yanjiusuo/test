package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class HttpAuthConfigDTO implements Serializable {

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
     * 应用编码
     */
    private String appCode;


    /**
     * 应用名称
     */
    private String appName;


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
     * 是否一键降级，1为生效，0为不生效
     */
    private String valid;

    /**
     * 是否强制鉴权，1为强制，0为不强制
     */
    private String forceValid;
    /**
     * 是否启用日志
     */
    private Boolean enableAuditLog ;
}
