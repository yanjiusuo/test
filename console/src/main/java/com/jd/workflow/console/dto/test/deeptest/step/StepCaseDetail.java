package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class StepCaseDetail {
    private Long id;

    private Long caseId;

    private String name;

    private Integer type;

    private String interfaceType;

    private String requestType;

    private String headerParam;

    private String fileName;

    private Long lineId;

    private Long interfaceId;

    private Long scriptId;

    private String interfaceName;

    private String methodName;

    private String alias;

    private String ipPort;

    private String token;

    private String inputParam;

    private String urlParam;

    private String inputParamType;

    private String bodyType;

    private Integer status;

    private String clientType;

    private String apiErp;

    private Integer isFileUpload;

    private Long callType;

    private Long overtime;

    private Long retryTime;

    private Integer matchType;

    private Long sleeptime;

    private String expectResult;

    private String expectDescription;

    private String stepDescription;

    private CompareRuleInfo compareRuleInfo;

    private BCaseDetail bCaseDetail;

    private String jdosApp;

    private Boolean isStatic;

    private String initParam;

    private String jarFile;

    private String initType;

    private String methodParam;

    private List<DataGroup> dataGroups;

    private Integer parallelNum;

    private Integer colorConfig;

    private Integer userAccount;
    /**
     * 压测标识
     */
    private Integer isForceBot;

    private String userAccountName;

    private Integer jdTls;
    /**
     * http请求加密参数
     */
    private String cipherIn;

    private String ownerErp;

    private String editorErp;

    private String ownerMail;

    private String editorMail;

    private String beforeScript;

    private String afterScript;

    private String jimdbField;

    private String expectResultField;

    private String ipPortField;

    private String excludeExpectResultField;
}
