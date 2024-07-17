package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateAppTenant {
    List<Long> ids;
    String tenantId;
}
