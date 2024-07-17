package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

import java.util.List;
@Data
public class JApiProjectResult {
    String statusCode;
    Integer projectListCount;
    List<JApiProjectInfo> projectList;

    public boolean isSuccess(){
        return "000000".equals(statusCode);
    }
}
