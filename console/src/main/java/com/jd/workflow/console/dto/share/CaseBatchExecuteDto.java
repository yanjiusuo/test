package com.jd.workflow.console.dto.share;

import lombok.Data;

@Data
public class CaseBatchExecuteDto {
    Long suiteId;
    String suiteName;
    /**
     * 用力集合id，以，分割
     */
    String caseIds;

    int timeInterval;
    int executeOrder; //1串行0并行
    int interrupted; //是否中断执行 1遇到错误中断执行0忽略错误继续执行，遇到错误时interrupted+100

    String emailAddress;//抄送邮箱

    String erp;//执行人erp
    String hostList;
    String sendType;
    String onlySendError;
}
