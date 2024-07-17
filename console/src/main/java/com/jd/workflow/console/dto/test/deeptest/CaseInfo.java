package com.jd.workflow.console.dto.test.deeptest;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/21
 */

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/21
 */
@Data
public class CaseInfo {
    /**
     * 用例ID
     */
    private Long id;
    /**
     * 模块ID
     */
    private Long lineId;
    /**
     * 所属用例集ID
     */
    private Long suiteId;
    /**
     * 用例名称
     */
    private String name;
    /**
     * 用例编排方式:0-列表，1-图形，2-代码
     */
    private Integer editWay;
    /**
     * 用例类型1-冒烟 2-回归 3-功能 4-联调
     */
    private String caseType;
    /**
     * 创建人
     */
    private String owner;
    /**
     * 优先级 0-P0， 1-P1，2-P2，3-P3
     */
    private Integer priority;
    /**
     * 边际人
     */
    private String editor;
    /**
     * 状态 1 删除 0未删除
     */
    private Integer status;
    /**
     * 描述
     */
    private String note;
    /**
     * 创建时间
     */
    private Date createTime;

    private Date updateTime;

    private long stepCount;

    private long latestStepId;
    /**
     * 运行类型
     */
    private Integer runType;

    private String cron;
    /**
     * 邮件地址
     */
    private String emailAddress;

    private int timeInterval;

    private int interrupted;

    private Integer noticeType;

    private String noticeInfo;

    private String onlySendError;

    private String phoneNumber;

    private int retryTime;

    private String caseName;

    private String source;
}
