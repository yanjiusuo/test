package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

import java.util.List;

/**
 * 冒烟用例
 */
@Data
public class SuiteDetail extends BaseTestEntityInfo {
    Long id;
    List<Long> ids;
    Long lineId;
    Long suiteId;
    /**
     * 所属用例集名称
     */
    String suiteName;
    /**
     * 用例名称
     */
    String name;
    /**
     *  编排方式：0：列表 1：图形，默认列表 2：代码编排
     */
    Integer editWay;
    /**
     * 用例类别 1：冒烟用例 2：回归用例 3：功能用例 4：联调用例 ,有多个用例类别时，用逗号分隔
     */
    String caseType;
    /**
     * 用例名称
     */
    String caseTypeName;
    /**
     * 步驟數量
     */
    Integer stepCount;
    String owner;
    Integer priority;

    List<CaseDetail> caseDetailList;
}
