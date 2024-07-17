package com.jd.workflow.console.service.param;

import com.jd.workflow.console.base.enums.ParamOptPositionEnum;
import com.jd.workflow.console.base.enums.ParamOptTypeEnum;
import com.jd.workflow.console.dto.ParamDepDto;
import com.jd.workflow.console.dto.ParamOptDto;
import com.jd.workflow.console.dto.requirement.AssertionResultDTO;
import com.jd.workflow.flow.core.output.BaseOutput;

import java.util.List;

/**
 * @description: 前置后置操作
 * @author: sunchao81
 * @Date: 2024-05-21
 */
public interface IParamBuilderOptService{
    /**
     * 1、前置操作
     * @param
     * @return
     */
    void preOpt(List<ParamOptDto> preOpt);

    /**
     * 2、渲染入参
     * @param
     * @return
     */
    List renderParam(List inputData);

    /**
     *
     * @param inputData
     * @return
     */
    String renderParam(String inputData);

    /**
     * 3、后置操作
     * @param
     * @return
     */
    void postOpt(List<ParamOptDto> postOpt);

    /**
     * 4、断言
     * @param
     * @return
     */
    List<AssertionResultDTO> assertionOpt(List<ParamOptDto> postOpt, BaseOutput output);

    /**
     * 参数依赖-2填充返回值
     * 2种模式
     * 简单模式 $<{1255}> 物料工具
     * 复杂模式 替换jsonPath 如 10#wlTool-255#out#$.data.productId  或  20#other#out#$.data.id
     * @param referId
     * @param result
     */
    void replaceValue(Long referId, Object result, ParamOptPositionEnum position);

    /**
     * 入参命中参数依赖关系
     * @return
     */
    List<ParamDepDto> hitParamDep(Long referId, ParamOptTypeEnum type, ParamOptPositionEnum position);
}
