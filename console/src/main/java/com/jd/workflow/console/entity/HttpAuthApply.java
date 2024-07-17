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
 * 鉴权标签管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "http_auth_apply",autoResultMap = true)
public class HttpAuthApply extends BaseEntity implements Serializable {

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
     * 鉴权标识
     */
    @TableField("`auth_code`")
    private String authCode;


    /**
     * token
     */
    @TableField("`token`")
    private String token;

    /**
     * 调用方应用编码
     */
    @TableField("`call_app_code`")
    private String callAppCode;


    /**
     * 调用方应用名称
     */
    @TableField("`call_app_name`")
    private String callAppName;

    /**
     * 是否推送ducc成功，0无效，1有效
     */
    @TableField("ducc_status")
    private Integer duccStatus;

    /**
     * 状态，审批状态
     */
    @TableField("ticket_status")
    private Integer ticketStatus;

    /**
     * 申请单ID
     */
    @TableField("ticket_id")
    private String ticketId;


}
