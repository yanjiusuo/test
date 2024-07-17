package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 * 日志消息体
 */
@Data
public class CamelLogDTO {

    private Long id;

    private String interfaceName;

    private String name;

    private String methodId;

    private Integer logLevel;

    private String publishUrl;

    private String version;

    private String logContent;

    private Date created;
}
