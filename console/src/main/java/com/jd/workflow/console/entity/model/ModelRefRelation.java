package com.jd.workflow.console.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 模型引用关系：存储模型被别的模型或者接口的引用关系
 */
@Data
@TableName(value = "model_ref_relation", autoResultMap = true)
public class ModelRefRelation extends BaseEntity {
    public static final int TYPE_MODEL = 1;
    public static final int TYPE_INTERFACE = 2;
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    // 引用类型：1-模型引用模型 2- 接口引用模型
    Integer type;
    /**
     * 应用id
     */
    Long appId;

    /**
     * 模型名称
     */
    String modelName;
    /**
     *  引用id: type=1的时候为模型id,type=2的时候为接口id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    Set<Long> relatedIds;

}