package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

/**
 * 冒烟用例
 */
@Data
public class CaseDetail extends BaseTestEntityInfo {
    Long id;
    Long caseId;
    String name;
    String serviceName;
    Integer type;
    String interfaceType;
    String requestType;
}
