package com.jd.workflow.console.dto.version;

import lombok.Data;

/**
 * 接口信息查询
 */
@Data
public class InterfaceInfoReq {

    private Long appId;

    private Long interfaceId;

    private String version;

    private String compareVersion;

    private Boolean onlyModify;

    //0 大版本 1 小版本
    private Integer addVersionType;

    // 版本描述
    private String addVersionDesc;

    //查询方法或方法比对
    private Long methodId;

    //demon比较
    private Boolean demonCompare;



}
