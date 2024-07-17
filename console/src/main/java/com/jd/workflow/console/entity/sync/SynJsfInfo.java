package com.jd.workflow.console.entity.sync;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * jsf平台同步的接口信息表
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("syn_jsf_info")
public class SynJsfInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * jsf接口名
     */
    @TableField("interface_name")
    private String interfaceName;

    /**
     * 用例总数
     */
    @TableField("provider_live")
    private Integer providerLive;

    /**
     * 用例执行成功数
     */
    @TableField("consumer_live")
    private Integer consumerLive;

    /**
     * 接口负责人
     */
    @TableField("owner_user")
    private String ownerUser;

    /**
     * 部门
     */
    @TableField("department")
    private String department;

    /**
     * 部门code
     */
    @TableField("departmentCode")
    private String departmentCode;

    /**
     * 部门code
     */
    @TableField("remark")
    private String remark;

    /**
     * 部门code
     */
    @TableField("created_time")
    private String createdTime;

    /**
     * 部门code
     */
    @TableField("modified_time")
    private String modifiedTime;

    /**
     * 藏经阁部门
     */
    @TableField("cjg_department")
    private String cjgDepartment;

    /**
     * 应用code
     */
    @TableField("app_code")
    private String appCode;

    /**
     * 应用负责人
     */
    @TableField("app_owner")
    private String appOwner;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

    /**
     * 创建时间
     */
    @TableField("created")
    private LocalDateTime created;

    /**
     * 修改时间
     */
    @TableField("modified")
    private LocalDateTime modified;

    /**
     * coding 地址
     */
    @TableField("code_address")
    private String codeAddress;

}

