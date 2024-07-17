package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * http鉴权配置管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "http_auth_config",autoResultMap = true)
public class HttpAuthConfig extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 站点
     */
    @TableField("`site`")
    private String site;

    /**
     * 应用编码
     */
    @TableField("`app_code`")
    private String appCode;


    /**
     * 应用名称
     */
    @TableField("`app_name`")
    private String appName;

    /**
     * 是否一键降级，1为生效，0为不生效
     */
    @TableField("`valid`")
    private String valid;


    /**
     * 是否强制鉴权，1为强制，0为不强制
     */
    @TableField("`force_valid`")
    private String forceValid;

    /**
     * 是否开启审计日志，1为开启，0为不开启
     */
    private Integer enableAuditLog;
}
