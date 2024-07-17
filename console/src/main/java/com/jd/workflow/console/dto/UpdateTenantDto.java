package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTenantDto {
    List<Long> interfaceIds;
    String tenantId;
}
