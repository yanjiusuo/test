package com.jd.workflow.console.entity.usecase;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * 详情叶信息
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Data
@Accessors(chain = true)
public class CaseSetExeLogInfo {
    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;

    /**
     * jsf别名
     */
    private String jsfAlias;

    /**
     * IP+端口
     */
    private String ip;

    /**
     * HTTP环境
     */
    private String httpEnv;

    /**
     * 新增代码行覆盖率
     */
    private String newCodeCoverage;

    /**
     * 分支名
     */
    private String branchName;

    /**
     * 新增代码覆盖率首页地址
     */
    private String newCodeCoverageIndexUrl;

    /**
     * 状态 1-用例待执行 2-用例执行中 3-覆盖率计算中 4-成功 5-失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 执行结果详情数据
     */
    private Page<ParamBuilderRecordDTO> page;



}
