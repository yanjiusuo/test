package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:24
 * @Description:参数分组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "flow_param_group")
public class FlowParamGroup extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 已关联参数List
     */
    @TableField(exist = false)
    private List<FlowParam> children;

}
