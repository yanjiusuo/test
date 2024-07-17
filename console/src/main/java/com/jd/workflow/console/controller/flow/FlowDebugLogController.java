package com.jd.workflow.console.controller.flow;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.DebugFlowReqDTO;
import com.jd.workflow.console.dto.flow.param.FlowDebugLogDto;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 调试日志 录制 & 回放
 */
@RestController
@Slf4j
@RequestMapping("/flowDebugLog")
@UmpMonitor
@Api(tags = "流程调试日志")
public class FlowDebugLogController {

    @Autowired
    FlowDebugLogService flowDebugLogService;

    @GetMapping(path = "pageList")
    @ApiOperation(value = "流程调试日志")
    public CommonResult<Page<FlowDebugLog>> pageList(String methodId, Long current, Long size) {
        return CommonResult.buildSuccessResult(flowDebugLogService.pageList(methodId, current, size));
    }

    /**
     * 批量移除调试记录
     *
     * @param ids
     * @return true为成功，false为失败
     */
    @PostMapping("/removeByIds")

    public CommonResult<Boolean> removeByIds(@RequestBody List<Long> ids) {
        Guard.notEmpty(ids, "批量移除调试记录时入参ID不能为空");
        return CommonResult.buildSuccessResult(flowDebugLogService.removeByIds(ids));
    }

    /**
     * 同步调试记录到mock平台
     *
     * @param ids
     * @return true为成功，false为失败
     */
    @PostMapping("/syncToMock")

    public CommonResult<Boolean> syncToMock(@RequestBody List<Long> ids) {
        Guard.notEmpty(ids, "同步调试记录到mock平台时入参ID不能为空");
        flowDebugLogService.syncToMock(ids);
        return CommonResult.buildSuccessResult(true);
    }

    @PostMapping("/queryDebugFlowLog")
    public CommonResult<Page<FlowDebugLog>> queryDebugFlowLog(@RequestBody DebugFlowReqDTO reqDTO) {
        log.info("FlowDebugLogController queryDebugFlowLog reqDTO={} ", JsonUtils.toJSONString(reqDTO));
        Guard.notNull(reqDTO.getMethodId(), "查询调试日志时入参流程ID不能为空");
        //service层
        Page<FlowDebugLog> flowDebugLogPage = flowDebugLogService.queryLogs(reqDTO);
        return CommonResult.buildSuccessResult(flowDebugLogPage);
    }

    @GetMapping(path = "/modifyDesc")
    public CommonResult<Boolean> modifyLogDesc(Long id,String desc) {
        log.info("FlowDebugLogController modifyLogDesc id={} , desc={}", id, desc);
        Guard.notNull(id, "修改日志描述信息时入参ID不能为空");
        if (Objects.nonNull(desc) && desc.length() > 200) {
            throw new BizException("修改日志描述信息时入参描述信息不能大于200");
        }
        return CommonResult.buildSuccessResult(flowDebugLogService.updateLogDesc(id, desc));
    }

    @GetMapping(path = "/removeLog")
    public CommonResult<Boolean> removeLog(Long id) {
        log.info("FlowDebugLogController removeLog id={} ", id);
        Guard.notNull(id, "删除调式日志时入参ID不能为空");
        return CommonResult.buildSuccessResult(flowDebugLogService.deleteLog(id));
    }

    @RequestMapping("/saveDebugLog")
    public CommonResult<Boolean> saveStepDebugLog(@RequestBody FlowDebugLogDto dto) {
        Guard.notEmpty(dto, "请求体不能为空");
        log.info("FlowDebugLogController saveStepDebugLog erp={},dto={} ", UserSessionLocal.getUser().getUserId(), JsonUtils.toJSONString(dto));
        flowDebugLogService.saveDebugLog(dto);
        return CommonResult.buildSuccessResult(true);

    }

    @GetMapping(path = "/cleanHistoryDebugLog")
    public CommonResult<Boolean> cleanHistoryDebugLog(String runKey) {
        log.info("FlowDebugLogController cleanHistoryDebugLog runKey={} ", runKey);
        Guard.notNull(runKey, "清理历史调式日志时参数非法");
        return CommonResult.buildSuccessResult(flowDebugLogService.manualClearHistoryLog(runKey));
    }

    /**
     * 快捷调用生成deeptest测试用例
     * @param ids 快捷调用历史记录id列表
     * @return
     */
    @PostMapping(path = "/initDeeptestCase")
    public CommonResult<List<Integer>> initDeeptestCase(@RequestBody List<Long> ids) {
        log.info("FlowDebugLogController initDeeptestCase id={} ", JSON.toJSONString(ids));
        Guard.notNull(ids, "入参ID不能为空");
        List<Integer> result = Lists.newArrayList();
        for (Long id : ids) {
            Integer caseId = flowDebugLogService.initDeeptestCase(id);
            result.add(caseId);
        }
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping(path = "/modifyTop")
    public CommonResult<Boolean> modifyTop(Long id, Integer topFlag) {
        log.info("FlowDebugLogController modifyTop id={} , desc={}", id, topFlag);
        Guard.notNull(id, "修改top不能为空");
        return CommonResult.buildSuccessResult(flowDebugLogService.updateTopFlag(id, topFlag));
    }
}
