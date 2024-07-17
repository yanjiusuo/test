package com.jd.workflow.console.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;

/**
 * 插件安装
 */
@TableName(value = "plugin_install_statistic",autoResultMap = true)
public class PluginInstallStatistic extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /**
     * 版本号
     */
    private String version;
    /**
     * 操作人
     */
    private String erp;
    /**
     * 操作类型：1-安装 2-更新
     */
    private Integer eventType;


}
