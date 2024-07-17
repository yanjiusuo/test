package com.jd.workflow.console.dto.requirement;

import lombok.Data;

/**
 * 行云需求成员信息
 */
@Data
public class DmMember {
    private Long id;
    private String dmCode;
    private String erp;
    private String createBy;
    private String createAt;
    private String modifyBy;
    private String modifyAt;
    private Integer yn;
}
