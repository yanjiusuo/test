package com.jd.workflow.console.dto.plugin;

import lombok.Data;

/**
 * idea插件配置条目
 */
@Data
public class PluginConfigItem {
    /**
     * 插件id
     */
    String id;
    String prefix;
    /**
     * 插件下载链接
     */
    String url;
    /**
     * 插件最新版本
     */
    String version;

    String sinceBuild;
    String untilBuild;
}
