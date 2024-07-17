package com.jd.workflow.console.dto.model;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class ApiModelReportDto {
    String className;
    String desc;
    List<JsonType> fields;
}
