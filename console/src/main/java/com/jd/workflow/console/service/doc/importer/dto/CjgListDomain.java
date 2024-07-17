package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

@Data
public class CjgListDomain {
    Long relId;
    Integer level;
    Integer domainType;
    String domainName;
    Long domainId;
    String domainCode;
}
