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
 * 类 名 称：InterfaceMethodGroup
 * 类 描 述：接口下方法分组
 * 创建时间：2022-11-08 16:17
 * 创 建 人：wangxiaofei8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_method_group",autoResultMap = true)
public class InterfaceMethodGroup extends BaseEntity implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口id,type为1时为接口id, type为2时，为RequirementInterfaceGroup的id,type为3时，为FlowStepInterfaceGroup的id
     */
    private Long interfaceId;
    /**
     * 分组英文名
     */
    private String enName;
    /**
     * 相关联的分组id,接口选中后，用来合并原有数据
     */
    private Long relatedGroupId;
    /**
     * 分组类型：{@link com.jd.workflow.console.dto.group.GroupTypeEnum}
     */
    private Integer type;

    /**
     * 分组名称
     */
    private String name;
    @TableField(exist = false)
    private Long relatedId;


}
