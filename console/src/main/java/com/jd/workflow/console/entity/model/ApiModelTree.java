package com.jd.workflow.console.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data

@TableName(value = "api_model_tree", autoResultMap = true)
public class ApiModelTree extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long appId;
    @TableField(value ="method_group_tree_model", typeHandler = JacksonTypeHandler.class)
    MethodGroupTreeModel treeModel;
    String groupLastVersion;


}
