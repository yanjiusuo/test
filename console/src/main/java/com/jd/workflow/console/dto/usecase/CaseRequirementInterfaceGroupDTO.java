package com.jd.workflow.console.dto.usecase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.console.entity.ITreeEntitySupport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 需求关联接口分组
 */
@Data
public class CaseRequirementInterfaceGroupDTO implements Serializable {

    /** 分组id主键 */
    private Long interfaceGroupId;

    /** 关联的需求id */
    private Long requirementId;

    /** 方法ID */
    private Long methodId;

    /** 节点类型：1-接口(叶子节点) 2-文件夹(非叶子节点) 3-项目(非叶子节点)。 */
    private String type;

    /** jsf 就是方法名称 */
    private String name;

    /** 前端使用（每一层节点的维一ID） */
    private String key;


    private List<CaseParamBuilderDTO> children;
}
