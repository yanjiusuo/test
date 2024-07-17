package com.jd.workflow.console.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class ColorUpdateParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * docUrl 文档路径包含两种：paas.jd.com 或者 j-api.jd.com
     */
    private String docUrl;
    /**
     * zone  pro:api.jd.com
     */
    private String zone;

    private String functionId;
    /**
     * type API_ADD("api_add", "API发布"),
     *      * API_DELETE("api_delete", "API删除"),
     *      * API_OFFLINE("api_offline", "API下线")
     */
    private String type;
}
