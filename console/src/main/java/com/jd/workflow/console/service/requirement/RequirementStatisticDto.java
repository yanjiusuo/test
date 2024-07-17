package com.jd.workflow.console.service.requirement;

import lombok.Data;

@Data
public class RequirementStatisticDto {
    String name;
    String relatedRequirementCode;
    Integer status;
    String creator;
    String members;
    String templateCode;

}
