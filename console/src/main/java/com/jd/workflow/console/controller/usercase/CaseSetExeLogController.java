package com.jd.workflow.console.controller.usercase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.dto.requirement.RequirementInfoDto;
import com.jd.workflow.console.entity.usecase.*;
import com.jd.workflow.console.service.requirement.RequirementStatisticDto;
import com.jd.workflow.console.service.usecase.CaseSetExeLogManager;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 用例集执行记录表 前端控制器
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@RestController
@RequestMapping("/caseSetExeLog")
public class CaseSetExeLogController {

    @Autowired
    private CaseSetExeLogService caseSetExeLogService;

    @Autowired
    private CaseSetExeLogManager caseSetExeLogManager;

    /**
     * 用例集执行-创建用例集执行记录
     *
     * @param createParam
     * @return
     */
    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody @Validated CreateCaseSetExeLogParam createParam) {
        Long id = caseSetExeLogService.createCaseSetExeLog(createParam);
        return CommonResult.buildSuccessResult(id);
    }


    /**
     * 用例集执行记录列表数据
     *
     * @param pageParam
     * @return
     */
    @PostMapping("/pageList")
    public CommonResult<IPage<CaseSetExeLogDTO>> pageList(@RequestBody @Validated PageCaseSetExeLogParam pageParam) {
        final Page<CaseSetExeLogDTO> caseSetExeLogDTOPage = caseSetExeLogService.pageList(pageParam);
        return CommonResult.buildSuccessResult(caseSetExeLogDTOPage);
    }


    /**
     * 通过Id删除用例集数据
     *
     * @param id
     * @return
     */
    @GetMapping("/delById")
    public CommonResult delById(Long id) {
        caseSetExeLogManager.delById(id);
        return CommonResult.buildSuccessResult(null);
    }


    /**
     * 用例集执行记录详情
     *
     * @param detailParam
     * @return
     */
    @PostMapping("/detail")
    public CommonResult<CaseSetExeLogInfo> detail(@RequestBody @Validated CaseSetExeLogDetailParam detailParam) {
        CaseSetExeLogInfo detail = caseSetExeLogService.detail(detailParam);
        return CommonResult.buildSuccessResult(detail);
    }

    /**
     * 重新执行
     * @param caseSetExeLogId
     * @return
     */
    @GetMapping("/reExecute")
    public CommonResult<Long> reExecute(@NotNull Long caseSetExeLogId) {
        Long newId = caseSetExeLogService.reExecute(caseSetExeLogId);
        return CommonResult.buildSuccessResult(newId);
    }

}
