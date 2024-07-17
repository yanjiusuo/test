package com.jd.workflow.console.dto.requirement;

import lombok.Data;

/**
 * @author yza
 * @description
 * @date 2024/5/21
 */
@Data
public class ParamBuilderRecordDTO {

    /**
     * 执行记录id
     */
    private Long id;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 接口名
     */
    private String interfaceName;

    /**
     * 执行状态  0 初始化 1 待执行 2 执行中 3 执行成功 4 执行失败
     */
    private Integer runStatus;

    /**
     * 执行状态描述
     */
    private String runStatusDesc;

    /**
     * 执行结果
     */
    private String runMsg;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法类型：1.http 3.jsf
     */
    private Integer methodType;

    /**
     * url
     */
    private String url;

}
