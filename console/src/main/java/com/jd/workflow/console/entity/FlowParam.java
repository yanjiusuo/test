package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:14
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "flow_param")
public class FlowParam extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 参数名
     */
    private String name;

    /**
     * 参数值
     */
    private String value;

    /**
     * 参数值表达式
     */
    @TableField(exist = false)
    private String exp_name;

    /**
     * 分组id
     */
    @TableField("`group_id`")
    private Long groupId;

    /**
     * 分组名称
     */
    @TableField(exist = false)
    private String groupName;

    /**
     * 参数描述
     */
    private String description;


    /**
     * 用户Code
     */
    @TableField(exist = false)
    private String userCode;

    /**
     * 用户名称
     */
    @TableField(exist = false)
    private String userName;
}
