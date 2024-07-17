package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 用例集中包含用例类型
 */
@AllArgsConstructor
public enum CaseTypeEnum {

    caseTypeJsf(0,"只有jsf用例"),
    caseTypeHttp(1,"只有http用例"),

    caseTypeJsfHttp(2,"二者都有");


    @Getter
    @Setter
    private Integer code;


    @Getter
    @Setter
    private String description;


}
