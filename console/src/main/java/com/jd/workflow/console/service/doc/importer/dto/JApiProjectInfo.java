package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

import java.util.List;

@Data
public class JApiProjectInfo {
    Integer isOpen;
    String projectCreateTime;
    String projectDes;
    Long projectID;
    JApiGroupSortTree apiGroupSortTree;
    String projectName;
    List<JApiProjectOwner> partners;
    String projectOwnerApp;
    String projectType;
    String projectUpdateTime;
    String projectVersion;
    Integer userType;
}
