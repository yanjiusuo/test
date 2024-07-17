package com.jd.workflow.console.dto.test;

import lombok.Data;

@Data
public class CaseEntity {
    public CaseEntity(){}
    public CaseEntity(Long moduleId, Long suiteGroupId) {
        this.moduleId = moduleId;
        this.suiteGroupId = suiteGroupId;
    }

    /**
     * 模块id
     */
    private Long moduleId;
    /**
     * 用例集id
     */
    private Long suiteGroupId;
}
