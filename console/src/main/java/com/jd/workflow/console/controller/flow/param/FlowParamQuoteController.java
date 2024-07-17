package com.jd.workflow.console.controller.flow.param;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.flow.param.*;
import com.jd.workflow.console.service.IFlowParamQuoteService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 16:56
 * @Description: 公共参数引用controller
 */
@RestController
@Slf4j
@RequestMapping("/flowParamQuote")
@UmpMonitor
@Api(tags = "公共参数引用")
public class FlowParamQuoteController {

    @Resource
    private IFlowParamQuoteService flowParamQuoteService;

    /**
     * 引用公共参数
     *
     * @param flowParamQuoteDTO
     * @return
     */
    @PostMapping("/quoteParam")
    @ApiOperation("引用公共参数")
    public CommonResult<Boolean> quoteParam(@RequestBody FlowParamQuoteDTO flowParamQuoteDTO) {
        log.info("FlowParamQuoteController quoteParam flowParamQuoteDTO={}", JSON.toJSONString(flowParamQuoteDTO));
        Guard.notNull(flowParamQuoteDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(flowParamQuoteDTO.getParamIds(), "公共参数id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamQuoteService.quoteParam(flowParamQuoteDTO));
    }


    /**
     * 取消引用公共参数
     *
     * @param flowParamQuoteDTO
     * @return
     */
    @PostMapping("/cancelQuoteParam")
    @ApiOperation("取消引用公共参数")
    public CommonResult<Boolean> cancelQuoteParam(@RequestBody FlowParamQuoteDTO flowParamQuoteDTO) {
        log.info("FlowParamQuoteController quoteParam flowParamQuoteDTO={}", JSON.toJSONString(flowParamQuoteDTO));
        Guard.notNull(flowParamQuoteDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(flowParamQuoteDTO.getParamIds(), "公共参数id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamQuoteService.cancelQuoteParam(flowParamQuoteDTO));
    }


    /**
     * 查询已引用的公共参数
     *
     * @param queryDTO
     * @return
     */
    @PostMapping("/queryQuoteParam")
    @ApiOperation("查询已引用的公共参数")
    public CommonResult<QueryParamQuoteResultDTO> queryQuoteParam(@RequestBody QueryParamQuoteReqDTO queryDTO) {
        log.info("FlowParamQuoteController queryQuoteParam queryDTO={}", JSON.toJSONString(queryDTO));
        Guard.notNull(queryDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamQuoteService.queryQuoteParam(queryDTO));
    }

    /**
     * 查询已引用的公共参数
     *
     * @param queryDTO
     * @return
     */
    @PostMapping("/queryQuoteParamForGroup")
    @ApiOperation("查询已引用的公共参数,按照参数所属分组归堆")
    public CommonResult<QueryParamQuoteForGroupResultDTO> queryQuoteParamForGroup(@RequestBody QueryParamQuoteReqDTO queryDTO) {
        log.info("FlowParamQuoteController queryQuoteParam queryDTO={}", JSON.toJSONString(queryDTO));
        Guard.notNull(queryDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamQuoteService.queryQuoteParamForGroup(queryDTO));
    }



    /**
     * 查询未被引用的公共参数
     *
     * @param queryDTO
     * @return
     */
    @PostMapping("/queryUnQuoteParam")
    @ApiOperation("查询未引用的公共参数")
    public CommonResult<QueryParamResultDTO> queryUnQuoteParam(@RequestBody QueryParamQuoteReqDTO queryDTO) {
        log.info("FlowParamQuoteController queryUnQuoteParam queryDTO={}", JSON.toJSONString(queryDTO));
        Guard.notNull(queryDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamQuoteService.queryUnQuoteParam(queryDTO));
    }

}
