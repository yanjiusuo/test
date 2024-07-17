package com.jd.workflow.console.dto.plugin;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2 
 */
@Data
public class HotFile {
    /**
     * name 文件名称
     */
    private String name;
    /**
     * content 文件内容,目前实际未文件的url地址
     */
    private String content;

    private boolean webAppResource;

    /**
     * 报名处
     */
    String packageName;
    /**
     * 文件名称
     */
    String className;

    public boolean isWebAppResource() {
        return webAppResource;
    }

    public void setWebAppResource(boolean webAppResource) {
        this.webAppResource = webAppResource;
    }

}
