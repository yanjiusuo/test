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
@TableName(value = "http_auth_apply_detail",autoResultMap = true)
public class HttpAuthApplyDetail extends BaseEntity implements Serializable {

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
     * 方法ID
     */
    @TableField("`method_id`")
    private Long methodId;

    /**
     * 方法编码
     */
    @TableField("`method_code`")
    private String methodCode;
    /**
     * 方法名称
     */
    @TableField("`method_name`")
    private String methodName;

    /**
     * 方法路径
     */
    @TableField("`path`")
    private String path;

    /**
     * 接口ID
     */
    @TableField("`interface_id`")
    private Long interfaceId;

    /**
     * 接口编码
     */
    @TableField("`interface_code`")
    private String interfaceCode;
    /**
     * 接口名称
     */
    @TableField("`interface_name`")
    private String interfaceName;

    /**
     * 申请单ID
     */
    @TableField("ticket_id")
    private String ticketId;

    /**
     * token
     */
    @TableField("`token`")
    private String token;
}
