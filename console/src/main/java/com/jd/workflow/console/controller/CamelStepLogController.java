package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.CamelLogConditionDTO;
import com.jd.workflow.console.dto.CamelLogListDTO;
import com.jd.workflow.console.dto.CamelLogQueryDTO;
import com.jd.workflow.console.dto.CamelLogReqDTO;
import com.jd.workflow.console.service.ICamelStepLogService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import io.swagger.annotations.ApiOperation;

/**
 * 日志查询
 */
@Slf4j
@RestController
@RequestMapping("/log")
@UmpMonitor
@Api(tags="调用日志")
public class CamelStepLogController {

    @Resource
    private ICamelStepLogService camelStepLogService;

    @PostMapping("/queryStepLog")
    @ApiOperation(value = "查询日志列表")
    public CommonResult<CamelLogListDTO> queryCamleStepLog(@RequestBody CamelLogReqDTO reqDTO) {
        log.info("CamelStepLogController queryStepLog reqDTO={} ", JsonUtils.toJSONString(reqDTO));
        //service层
        CamelLogListDTO camelLogListDTO = camelStepLogService.queryCamleStepLog(reqDTO);
        return CommonResult.buildSuccessResult(camelLogListDTO);
    }

    @PostMapping("/queryInterfaceCondition")
    @ApiOperation(value = "查询条件接口列表")
    public CommonResult<CamelLogConditionDTO> queryLogInterfaceCondition(@RequestBody CamelLogReqDTO reqDTO) {
        log.info("CamelStepLogController queryInterfaceCondition reqDTO={} ", JsonUtils.toJSONString(reqDTO));
        //service层
        CamelLogConditionDTO camelLogConditionDTO = camelStepLogService.queryLogInterfaceCondition(reqDTO);
        return CommonResult.buildSuccessResult(camelLogConditionDTO);
    }

    @PostMapping("/queryMethodCondition")
    @ApiOperation(value = "查询条件方法列表")
    public CommonResult<CamelLogConditionDTO> queryLogMethodCondition(@RequestBody CamelLogQueryDTO reqDTO) {
        log.info("CamelStepLogController queryMethodCondition reqDTO={} ", JsonUtils.toJSONString(reqDTO));
        Guard.notNull(reqDTO.getInterfaceId(), "查询发布的方法列表interfaceId不能为空");
        //service层
        CamelLogConditionDTO camelLogConditionDTO = camelStepLogService.queryLogMethodCondition(reqDTO);
        return CommonResult.buildSuccessResult(camelLogConditionDTO);
    }

    @PostMapping("/queryVersionCondition")
    @ApiOperation(value = "查询条件版本列表")
    public CommonResult<CamelLogConditionDTO> queryLogVersionCondition(@RequestBody CamelLogReqDTO reqDTO) {
        log.info("CamelStepLogController queryLogVersionCondition reqDTO={} ", JsonUtils.toJSONString(reqDTO));
        Guard.notNull(reqDTO.getMethodId(), "查询发布的版本列表method不能为空");
        //service层
        CamelLogConditionDTO camelLogConditionDTO = camelStepLogService.queryLogVersionCondition(reqDTO);
        return CommonResult.buildSuccessResult(camelLogConditionDTO);
    }
}
