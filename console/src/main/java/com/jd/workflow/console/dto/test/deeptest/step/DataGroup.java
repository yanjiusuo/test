package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class DataGroup {
    private Long id;

    private String name;

    private Integer sequence;

    private Boolean isUsed;

    private Long caseDetailId;

    private String urlParam;

    private String inputParamType;

    private String inputParam;

    private Integer matchType;

    private String expectResult;

    private String compareRules;

    private String scriptType;

    private String script;

    private Integer ignoreNull;

    private Integer ignoreOrder;

    private String ignorePath;

    private CompareRuleInfo compareRuleInfo;
}
