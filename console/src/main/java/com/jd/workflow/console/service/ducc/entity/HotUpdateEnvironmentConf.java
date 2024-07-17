package com.jd.workflow.console.service.ducc.entity;

import lombok.Data;

/**
 * 热部署环境配置
 */
@Data
public class HotUpdateEnvironmentConf {
    /**
     * 名称
     */
    private String name;
    /**
     * 编码
     */
    private String code;
    /**
     * 1==内部 0 外部
     */
    private Integer type;


    /**
     * 域名
     */
    private String host;

    /**
     * 如果是外网，就带上ip，走代理模式访问
     */
    private String hostIp;

    /**
     * 中转桶名
     */
    private String bucketName;
}
