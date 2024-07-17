package com.jd.workflow.console.entity.jagile;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 行云jdos部署记录表
 * </p>
 *
 * @author zhaojingchun
 * @since 2023-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wf_app_jagile_deploy_info")
public class AppJagileDeployInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 系统名
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 应用名
     */
    @TableField("app_name")
    private String appName;

    /**
     * 应用负责人
     */
    @TableField("app_owner")
    private String appOwner;

    /**
     * 应用负责人姓名
     */
    @TableField("app_owner_name")
    private String appOwnerName;

    /**
     * 应用所属部门
     */
    @TableField("app_dept_fullname")
    private String appDeptFullname;

    /**
     * 环境
     */
    @TableField("environment")
    private String environment;

    /**
     * 镜像名称
     */
    @TableField("image_name")
    private String imageName;

    /**
     * Git地址
     */
    @TableField("git_project")
    private String gitProject;

    /**
     * 分支
     */
    @TableField("branch")
    private String branch;

    /**
     * commit
     */
    @TableField("git_version")
    private String gitVersion;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;

    /**
     * 发布编号
     */
    @TableField("apply_number")
    private String applyNumber;

    /**
     * 组件code
     */
    @TableField("pmp_desc")
    private String pmpDesc;

    /**
     * 申请人erp
     */
    @TableField("applicant_erp")
    private String applicantErp;

    /**
     * 申请人名称
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 需求code
     */
    @TableField("demand_code")
    private String demandCode;


}
