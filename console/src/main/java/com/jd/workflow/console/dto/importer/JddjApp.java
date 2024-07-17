package com.jd.workflow.console.dto.importer;

import lombok.Data;

import java.util.List;
@Data
public class JddjApp {
    String appCode;
    String appName;
    String sysCode;
    List<String> envList;
}
