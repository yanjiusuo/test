package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

import java.util.List;

@Data
public class CjgQueryDomainRecord {
    Long id;
    List<CjgListDomain> domains;
}
