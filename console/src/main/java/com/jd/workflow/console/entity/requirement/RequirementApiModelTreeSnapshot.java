package com.jd.workflow.console.entity.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/31
 */

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/31 
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
@Data

@TableName(value = "requirement_api_model_tree_snapshot", autoResultMap = true)
public class RequirementApiModelTreeSnapshot extends BaseEntity {


    private Long id;
    private Long appId;
    @TableField(value ="method_group_tree_model", typeHandler = JacksonTypeHandler.class)
    MethodGroupTreeModel treeModel;
    String groupLastVersion;
    /**
     * 关联的需求id
     */
    private String requirementId;

}
