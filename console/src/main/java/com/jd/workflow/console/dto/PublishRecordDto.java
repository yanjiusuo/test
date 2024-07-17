package com.jd.workflow.console.dto;

import lombok.Data;

@Data
public class PublishRecordDto {
    /**
     * camel配置文件
     */
    String config;
    /**
     * 方法id
     */
    String methodId;
    /**
     * 发布的版本，每次版本的话发布记录都有一个版本号
     */
    String publishVersion;
    /**
     * 集群code
     */
    String clusterCode;
}
