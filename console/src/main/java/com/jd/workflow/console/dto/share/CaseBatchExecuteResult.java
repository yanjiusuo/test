package com.jd.workflow.console.dto.share;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Date;

@Data
public class CaseBatchExecuteResult {

    private Long id;

    private String name;
    //关联任务
    private Long taskInfoId;

    //关联用例执行日志
    private Long taskExecuteLogId;

    //关联用例接口id
    private Long caseDetailId;

    //关联用例名称
    private String caseName;

    //关联用例ID
    private Long caseId;

    //执行入参
    private String execParam;

    private String dataGroupName;

    //执行内容
    private String execContent;

    //实际结果
    private String result;

    //期望结果
    private String expectResult;

    //匹配类型
    private int matchType;

    //匹配结果
    private String matchResult;

    //不匹配项
    private String errorReason;

    private String createTime;

    private String createTimeStr;

    private int matchOrder;

    private String preResult;

    private String afterResult;

    private Integer editWay;

    private Integer typeFramework;

}
