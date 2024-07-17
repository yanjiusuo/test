package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
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
@TableName(value = "app_info_members")
public class AppInfoMembers extends BaseEntity implements Serializable {

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
     * 应用id
     */
    private Long appId;

    /**
     * erp
     */
    private String erp;

    /**
     * 角色类型
     * @see AppUserTypeEnum
     */
    private Integer roleType;


}
