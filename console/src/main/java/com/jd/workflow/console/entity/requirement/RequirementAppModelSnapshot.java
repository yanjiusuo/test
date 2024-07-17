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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
@TableName(value = "requirement_app_model_snapshot", autoResultMap = true)
public class RequirementAppModelSnapshot extends BaseEntity {

    Long id;
    String name; // 类名
    Long appId;
    @TableField("`desc`")
    String desc;

    Integer autoReport;
    @TableField(typeHandler = JacksonTypeHandler.class)
    JsonType content; // 自动上报的内容
    @TableField(typeHandler = JacksonTypeHandler.class)
    List<String> refNames;// 引用的模型列表，包含递归引用的

    /**
     * 需求id
     */
    private String requirementId;
    /**
     * 模型id
     */
    private Long modelId;
}
