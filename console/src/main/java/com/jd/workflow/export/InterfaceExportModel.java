package com.jd.workflow.export;

import lombok.Data;

import java.util.List;

@Data
public class InterfaceExportModel {
    List<GroupExportModel> groups;
    String docType;
    String docInfo;
}
