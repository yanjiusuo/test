package com.jd.workflow.console.entity.requirement;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.RequirementDocConfig;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.console.entity.ITreeEntitySupport;
import lombok.Data;

import java.io.Serializable;

/**
 * 步骤关联接口表
 */
@Data
@TableName(value = "flow_step_interface_group",autoResultMap = true)
public class FlowStepInterfaceGroup extends BaseEntityNoDelLogic implements ITreeEntitySupport,Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 接口类型
     */
    private int interfaceType;
    /**
     * 需求id
     */
    private String requirementId;

    /**
     * 流程步骤id
     */
    private String flowStepId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private MethodGroupTreeModel sortGroupTree;

    /**
     * 当前分组版本，默认当前时间戳
     */
    @TableField
    private String groupLastVersion;
    /**
     * 配置信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    RequirementDocConfig docConfig;
    /**
     * markdown文档信息
     */
    String docInfo;
}
