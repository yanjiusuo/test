package com.jd.workflow.console.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

import java.util.Map;
@Data
@TableName(value = "plugin_statistic",autoResultMap = true)
public class PluginStatistic extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    String remoteIp;
    /**
     * 类型
     */
    String type;
    /**
     * 当前用户名
     */
    String userName;

    String channel;
    /**
     * 操作人
     */
    String erp;
    /**
     * 统计信息
     */
    String statisticData;
    /**
     * 额外信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    Map<String,Object> extInfos;

    private String pluginVersion;
}
