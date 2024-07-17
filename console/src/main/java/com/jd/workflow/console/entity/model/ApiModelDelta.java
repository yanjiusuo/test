package com.jd.workflow.console.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "api_model_delta", autoResultMap = true)
public class ApiModelDelta extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    String name;
    @TableField("`desc`")
    String desc;

    Long apiModelId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    JsonType content;


}
