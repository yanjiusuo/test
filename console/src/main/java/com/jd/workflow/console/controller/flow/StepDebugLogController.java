package com.jd.workflow.console.controller.flow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.entity.debug.StepDebugLog;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.console.service.debug.StepDebugLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;

/**
 * 调试日志 录制 & 回放
 */
@RestController
@Slf4j
@RequestMapping("/stepDebugLog")
@UmpMonitor
@Api(tags="步骤调试日志")
public class StepDebugLogController {

    @Autowired
    StepDebugLogService stepDebugLogService;

    /**
     * 获取调试日志
     * @param stepId 步骤id
     * @param methodId 方法id
     * @param current 分页当前页
     * @param size 分页size
     * @return
     */
    @GetMapping(path = "pageList")
    @ApiOperation(value = "步骤调试日志")
    public CommonResult<Page<StepDebugLog>> pageList(String stepId, String methodId, Long current, Long size) {
        return CommonResult.buildSuccessResult(stepDebugLogService.pageList(stepId, methodId, current, size));
    }
}
