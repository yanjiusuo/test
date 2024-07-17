package com.jd.workflow.console.dto.version;

import lombok.Data;

import java.util.Date;

/**
 * 接口信息
 */
@Data
public class InterfaceInfo {

    private Long appId;

    private Long interfaceId;

    private String version;

    private Integer type;

    private String name;

    private String serviceCode;

    private String versionDesc;

    private Date versionCreated;

    private String pin;

    private String pinName;

    private String department;


}
