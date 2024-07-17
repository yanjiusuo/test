package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 项目名称：parent
 * 类 名 称：AppInfo
 * 类 描 述：应用
 * 创建时间：2022-11-16 14:44
 * 创 建 人：wangxiaofei8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "app_info")
public class AppInfo extends BaseEntity implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用code
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;


    private String appType;

    /**
     * 调用级别 0 接口 1方法
     */
    private String authLevel;

    /**
     * 秘钥
     */
    private String appSecret;

    /**
     * 成员
     */
    private String members;

    /**
     * 应用描述
     */
    @TableField("`desc`")
    private String desc;

    /**
     * 关联cjgId
     */
    private String cjgAppId;

    /**
     * 租户id
     */
    private String tenantId;
    /**
     * jdos应用编码
     */
    private String jdosAppCode;
    /**
     * 站点
     */
    private String site;

    /**
     * 部门信息
     */
    private String dept;

    private String cjgBusinessDomainTrace;

    private String cjgProductTrace;
}
