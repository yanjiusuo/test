package com.jd.workflow.console.entity.manage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.manage.FilterRuleConfig;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/**
 *  应用接口调试错误日志
 */
@Data
@TableName(autoResultMap = true)
public class AppDebugErrorConfig extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 应用编码
     * @hidden
     */
    private String appCode;
    /**
     * 配置信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FilterRuleConfig config;

}
