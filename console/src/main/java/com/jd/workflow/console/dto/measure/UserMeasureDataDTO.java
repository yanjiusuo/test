package com.jd.workflow.console.dto.measure;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yza
 * @description
 * @date 2024/1/15
 */
@Data
public class UserMeasureDataDTO {

    /**
     * 用户名
     */
    private String erp;

    /**
     * 部门
     */
    private String department;

    /**
     * 接口上报次数
     */
    private Integer interfaceReportNum;

    /**
     * 快捷调用成功次数
     */
    private Integer quickCallSuccessNum;

    /**
     * 快捷调用失败次数
     */
    private Integer quickCallFailNum;

    /**
     * mock模版数
     */
    private Integer mockTemplateNum;

    /**
     * 快捷调用生成mock模版数
     */
    private Integer quickCallMockTemplateNum;

    /**
     * 需求空间数
     */
    private Integer requirementInfoNum;

    /**
     * 需求空间绑定需求数
     */
    private Integer requirementInfoBindNum;

    /**
     * 热更新成功次数
     */
    private BigDecimal hotswapSuccessBigNum;

    /**
     * 热更新成功率
     */
    private String hotswapSuccessRatio;

    /**
     * 热更新平均时长
     */
    private String hotswapSuccessAvgTime;

    /**
     * 远程调试次数
     */
    private Integer remoteDebugNum;

}
