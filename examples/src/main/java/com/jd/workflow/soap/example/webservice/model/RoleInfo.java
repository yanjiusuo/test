package com.jd.workflow.soap.example.webservice.model;

import lombok.Data;

@Data
public class RoleInfo {
    Long id;
    String roleName;
    String roleDesc;
    String level;
    String createBy;
    String createDate;
}
