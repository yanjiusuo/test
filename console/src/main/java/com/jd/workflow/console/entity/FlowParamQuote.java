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
 * @Date: 2023/3/21 17:16
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_flow_param")
public class FlowParamQuote extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口id
     */
    @TableField("`interface_id`")
    private Long interfaceId;

    /**
     * 公共参数id，关联公共参数
     */
    @TableField("`flowParam_id`")
    private Long flowParamId;

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
     * 参数名称
     */
    @TableField(exist = false)
    private String name;

    /**
     * 参数值
     */
    @TableField(exist = false)
    private String value;

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
