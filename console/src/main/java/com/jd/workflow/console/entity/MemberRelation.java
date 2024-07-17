package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 接口成员关联表
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_relation")
public class MemberRelation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户唯一标识
     */
    private String userCode;

    /**
     * 资源id
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 根据枚举控制
     * {@link com.jd.workflow.console.base.enums.ResourceTypeEnum}
     */
    @TableField("resource_type")
    private Integer resourceType;

    /**
     * 资源角色 0-无 1-租户管理员 2负责人 3成员
     * {@link com.jd.workflow.console.base.enums.ResourceRoleEnum}
     */
    @TableField("resource_role")
    private Integer resourceRole;


    @TableField(exist = false)
    String userName;

    @TableField(exist = false)
    String deptName;

}
