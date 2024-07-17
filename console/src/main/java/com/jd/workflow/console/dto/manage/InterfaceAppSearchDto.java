package com.jd.workflow.console.dto.manage;

import lombok.Data;

import java.util.List;

@Data
public class InterfaceAppSearchDto {
    Integer type;
    String name;
    Long appId;
    List<Long> interfaceIds;
    Long current;
    Long size;
}
