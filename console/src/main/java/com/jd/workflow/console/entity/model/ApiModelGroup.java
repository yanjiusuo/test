package com.jd.workflow.console.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "api_model_group", autoResultMap = true)
public class ApiModelGroup extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long appId;
    String name;
    // 实际的包名
    String fullName;
    String enName;

}
